package com.simba.elasticjob.executor;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.error.handler.JobErrorHandler;
import com.simba.elasticjob.event.JobExecutionEvent;
import com.simba.elasticjob.event.JobStatusTraceEvent;
import com.simba.elasticjob.exception.ExceptionUtils;
import com.simba.elasticjob.exception.JobExecutionEnvironmentException;
import com.simba.elasticjob.executor.context.ExecutorContext;
import com.simba.elasticjob.executor.item.JobItemExecutor;
import com.simba.elasticjob.executor.item.JobItemExecutorFactory;
import com.simba.elasticjob.internal.listener.ShardingContexts;
import com.simba.elasticjob.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @Description  ElasticJob 执行器.
 * @Author yuanjx3
 * @Date 2021/1/22 8:28
 * @Version V1.0
 **/
public final class ElasticJobExecutor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ElasticJob elasticJob;

    private final JobFacade jobFacade;

    private final JobItemExecutor jobItemExecutor;

    private final ExecutorContext executorContext;

    private final Map<Integer, String> itemErrorMessages;

    public ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(elasticJob, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(elasticJob.getClass()));
    }

    public ElasticJobExecutor(final String type, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(null, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(type));
    }

    private ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final JobItemExecutor jobItemExecutor) {
        this.elasticJob = elasticJob;
        this.jobFacade = jobFacade;
        this.jobItemExecutor = jobItemExecutor;
        executorContext = new ExecutorContext(jobFacade.loadJobConfiguration(true));
        itemErrorMessages = new ConcurrentHashMap<>(jobConfig.getShardingTotalCount(), 1);
    }

    /**
     * Execute job.
     */
    public void execute() {
        JobConfiguration jobConfig = jobFacade.loadJobConfiguration(true);
        executorContext.reloadIfNecessary(jobConfig);
        JobErrorHandler jobErrorHandler = executorContext.get(JobErrorHandler.class);
        try {
            // 检查作业执行环境（作业服务器与注册中心之间时差超过最大允许时差时，
            // 会抛出JobExecutionEnvironmentException异常，中断执行）
            jobFacade.checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException cause) {
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
        // 这里会触发执行作业分片
        ShardingContexts shardingContexts = jobFacade.getShardingContexts();
        // 往总线上推送作业状态为 TASK_STAGING 的事件
        jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), JobStatusTraceEvent.State.TASK_STAGING, String.format("Job '%s' execute begin.", jobConfig.getJobName()));
        // 如果没有作业分片在执行，设置未触发标识符
        if (jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())) {
            //设置未触发标识符成功后，往总线上推送作业状态为 TASK_FINISHED 的事件
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), JobStatusTraceEvent.State.TASK_FINISHED, String.format(
                    "Previous job '%s' - shardingItems '%s' is still running, misfired job will start after previous job completed.", jobConfig.getJobName(),
                    shardingContexts.getShardingItemParameters().keySet()));
            return;
        }
        try {
            // 作业分片执行前设置
            jobFacade.beforeJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
        execute(jobConfig, shardingContexts, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER);
        while (jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.clearMisfire(shardingContexts.getShardingItemParameters().keySet());
            execute(jobConfig, shardingContexts, JobExecutionEvent.ExecutionSource.MISFIRE);
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
    }

    private void execute(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final JobExecutionEvent.ExecutionSource executionSource) {
        if (shardingContexts.getShardingItemParameters().isEmpty()) {
            // 如果作业分片为空，往总线上推送作业状态为 TASK_FINISHED 的事件，然后返回
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), JobStatusTraceEvent.State.TASK_FINISHED, String.format("Sharding item for job '%s' is empty.", jobConfig.getJobName()));
            return;
        }
        // 注册作业开始执行
        jobFacade.registerJobBegin(shardingContexts);
        String taskId = shardingContexts.getTaskId();
        // 往总线上推送作业状态为 TASK_RUNNING 的事件
        jobFacade.postJobStatusTraceEvent(taskId, JobStatusTraceEvent.State.TASK_RUNNING, "");
        try {
            process(jobConfig, shardingContexts, executionSource);
        } finally {
            // TODO Consider increasing the status of job failure, and how to handle the overall loop of job failure
            jobFacade.registerJobCompleted(shardingContexts);
            if (itemErrorMessages.isEmpty()) {
                jobFacade.postJobStatusTraceEvent(taskId, JobStatusTraceEvent.State.TASK_FINISHED, "");
            } else {
                jobFacade.postJobStatusTraceEvent(taskId, JobStatusTraceEvent.State.TASK_ERROR, itemErrorMessages.toString());
            }
        }
    }

    /** 执行作业 **/
    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final JobExecutionEvent.ExecutionSource executionSource) {
        // 获取作业分片集合
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        // 如果分片数为1，直接进行处理
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, item);
            process(jobConfig, shardingContexts, item, jobExecutionEvent);
            return;
        }
        // 分片数大于1，初始化分片数大小的计数器
        //然后创建线程池，将处理线程提交至线程池执行
        CountDownLatch latch = new CountDownLatch(items.size());
        for (int each : items) {
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, each);
            ExecutorService executorService = executorContext.get(ExecutorService.class);
            if (executorService.isShutdown()) {
                return;
            }
            executorService.submit(() -> {
                try {
                    process(jobConfig, shardingContexts, each, jobExecutionEvent);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final int item, final JobExecutionEvent startEvent) {
        jobFacade.postJobExecutionEvent(startEvent);
        log.trace("Job '{}' executing, item is: '{}'.", jobConfig.getJobName(), item);
        JobExecutionEvent completeEvent;
        try {
            // 这个jobItemExecutor就是前面工厂类根据我的MyJob类型找到的 SimpleJobExecutor，执行的就是SimpleJobExecutor的execute方法
            // 这里的elasticJob就是我前面传入的MyJob作业
            jobItemExecutor.process(elasticJob, jobConfig, jobFacade, shardingContexts.createShardingContext(item));
            completeEvent = startEvent.executionSuccess();
            log.trace("Job '{}' executed, item is: '{}'.", jobConfig.getJobName(), item);
            // 至此，一次作业分片执行完成
            jobFacade.postJobExecutionEvent(completeEvent);
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            completeEvent = startEvent.executionFailure(ExceptionUtils.transform(cause));
            jobFacade.postJobExecutionEvent(completeEvent);
            itemErrorMessages.put(item, ExceptionUtils.transform(cause));
            JobErrorHandler jobErrorHandler = executorContext.get(JobErrorHandler.class);
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
    }

    /**
     * Shutdown executor.
     */
    public void shutdown() {
        executorContext.shutdown();
    }
}
