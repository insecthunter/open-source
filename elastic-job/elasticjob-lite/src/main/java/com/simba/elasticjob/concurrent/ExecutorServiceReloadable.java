package com.simba.elasticjob.concurrent;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.Reloadable;
import com.simba.elasticjob.executor.ReloadablePostProcessor;
import com.simba.elasticjob.handler.threadpool.JobExecutorServiceHandlerFactory;
import com.simba.elasticjob.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * @Description Executor Service Reloadable
 * @Author yuanjx3
 * @Date 2021/1/22 8:53
 * @Version V1.0
 **/
public class ExecutorServiceReloadable implements Reloadable<ExecutorService>, ReloadablePostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String jobExecutorServiceHandlerType;

    private ExecutorService executorService;

    @Override
    public void init(final JobConfiguration jobConfig) {
        jobExecutorServiceHandlerType = StringUtils.isNullOrEmpty(jobConfig.getJobExecutorServiceHandlerType())
                ? JobExecutorServiceHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobExecutorServiceHandlerType();
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobExecutorServiceHandlerType).createExecutorService(jobConfig.getJobName());
    }

    @Override
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        String newJobExecutorServiceHandlerType = StringUtils.isNullOrEmpty(jobConfig.getJobExecutorServiceHandlerType())
                ? JobExecutorServiceHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobExecutorServiceHandlerType();
        if (newJobExecutorServiceHandlerType.equals(jobExecutorServiceHandlerType)) {
            return;
        }
        log.debug("JobExecutorServiceHandler reload occurred in the job '{}'. Change from '{}' to '{}'.", jobConfig.getJobName(), jobExecutorServiceHandlerType, newJobExecutorServiceHandlerType);
        reload(newJobExecutorServiceHandlerType, jobConfig.getJobName());
    }

    private void reload(final String jobExecutorServiceHandlerType, final String jobName) {
        executorService.shutdown();
        this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobExecutorServiceHandlerType).createExecutorService(jobName);
    }

    @Override
    public ExecutorService getInstance() {
        return executorService;
    }

    @Override
    public void close() {
        Optional.ofNullable(executorService).ifPresent(ExecutorService::shutdown);
    }

    @Override
    public String getType() {
        return ExecutorService.class.getName();
    }
}
