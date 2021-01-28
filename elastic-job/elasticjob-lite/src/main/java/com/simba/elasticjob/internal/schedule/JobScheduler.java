package com.simba.elasticjob.internal.schedule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.error.JobErrorHandlerPropertiesValidator;
import com.simba.elasticjob.exception.JobSystemException;
import com.simba.elasticjob.executor.ElasticJobExecutor;
import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.internal.guarantee.GuaranteeService;
import com.simba.elasticjob.internal.listener.AbstractDistributeOnceElasticJobListener;
import com.simba.elasticjob.internal.listener.ElasticJobListener;
import com.simba.elasticjob.internal.setup.JobClassNameProviderFactory;
import com.simba.elasticjob.internal.setup.SetUpFacade;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;
import com.simba.elasticjob.tracing.api.TracingConfiguration;
import com.simba.elasticjob.tracing.listener.ElasticJobListenerFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @Description 任务调度器
 * @Author yuanjx3
 * @Date 2021/1/13 20:23
 * @Version V1.0
 **/
public class JobScheduler {
    Logger log = LoggerFactory.getLogger(getClass());

    static {
        ElasticJobServiceLoader.registerTypedService(JobErrorHandlerPropertiesValidator.class);
    }
    private static final String JOB_EXECUTOR_DATA_MAP_KEY = "jobExecutor";

    private final CoordinatorRegistryCenter regCenter;
    private final JobConfiguration jobConfig;
    private final JobScheduleController jobScheduleController;
    private final SetUpFacade setUpFacade;
    private final SchedulerFacade schedulerFacade;
    private final LiteJobFacade liteJobFacade;
    private final ElasticJobExecutor jobExecutor;

    public JobScheduler(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(null != elasticJob, "Elastic job cannot be null.");
        this.regCenter = regCenter;
        // 获取作业监听器集合
        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfig);
        // 初始化代理操作门面类
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), jobListeners);
        // 获取传入的elasticjob的类名
        String jobClassName = JobClassNameProviderFactory.getProvider().getJobClassName(elasticJob);
        // 设置作业配置信息（更新作业信息到注册中心，并返回最新的作业配置信息）
        this.jobConfig = setUpFacade.setUpJobConfiguration(jobClassName, jobConfig);
        // 初始化调度管理器
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        // 初始化 Lite job 外观类
        liteJobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), jobListeners, findTracingConfiguration().orElse(null));
        //校验作业属性
        validateJobProperties();
        // 初始化 ElasticJob 作业执行器
        jobExecutor = new ElasticJobExecutor(elasticJob, this.jobConfig, liteJobFacade);
        // 设置分布式单次执行作业监听器（如果jobListeners集合中有AbstractDistributeOnceElasticJobListener类型的监听器，为其设置单次执行服务）
        setGuaranteeServiceForElasticJobListeners(regCenter, jobListeners);
        // 创建任务调度控制器
        jobScheduleController = createJobScheduleController();
    }

    public JobScheduler(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(elasticJobType), "Elastic job type cannot be null or empty.");
        this.regCenter = regCenter;
        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfig);
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), jobListeners);
        this.jobConfig = setUpFacade.setUpJobConfiguration(elasticJobType, jobConfig);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        liteJobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), jobListeners, findTracingConfiguration().orElse(null));
        validateJobProperties();
        jobExecutor = new ElasticJobExecutor(elasticJobType, this.jobConfig, liteJobFacade);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobListeners);
        jobScheduleController = createJobScheduleController();
    }

    private Collection<ElasticJobListener> getElasticJobListeners(final JobConfiguration jobConfig) {
        return jobConfig.getJobListenerTypes().stream()
                .map(type -> ElasticJobListenerFactory.createListener(type).orElseThrow(() -> new IllegalArgumentException(String.format("Can not find job listener type '%s'.", type))))
                .collect(Collectors.toList());
    }

    private Optional<TracingConfiguration<?>> findTracingConfiguration() {
        return jobConfig.getExtraConfigurations().stream().filter(each -> each instanceof TracingConfiguration).findFirst().map(extraConfig -> (TracingConfiguration<?>) extraConfig);
    }

    private void validateJobProperties() {
        validateJobErrorHandlerProperties();
    }

    private void validateJobErrorHandlerProperties() {
        if (null != jobConfig.getJobErrorHandlerType()) {
            ElasticJobServiceLoader.newTypedServiceInstance(JobErrorHandlerPropertiesValidator.class, jobConfig.getJobErrorHandlerType(), jobConfig.getProps())
                    .ifPresent(validator -> validator.validate(jobConfig.getProps()));
        }
    }

    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final Collection<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig.getJobName());
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }

    /** 创建任务调度控制器 **/
    private JobScheduleController createJobScheduleController() {
        // 实例化任务调度器
        JobScheduleController result = new JobScheduleController(createScheduler(), createJobDetail(), getJobConfig().getJobName());
        // 往作业注册中心的集合类中添加新的作业
        JobRegistry.getInstance().registerJob(getJobConfig().getJobName(), result);
        // 注册启动信息
        registerStartUpInfo();
        return result;
    }

    private Scheduler createScheduler() {
        Scheduler result;
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(getQuartzProps());
            result = factory.getScheduler();
            result.getListenerManager().addTriggerListener(schedulerFacade.newJobTriggerListener());
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
        return result;
    }

    private Properties getQuartzProps() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", getJobConfig().getJobName());
        result.put("org.quartz.jobStore.misfireThreshold", "1");
        result.put("org.quartz.plugin.shutdownhook.class", JobShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }

    private JobDetail createJobDetail() {
        JobDetail result = JobBuilder.newJob(LiteJob.class).withIdentity(getJobConfig().getJobName()).build();
        result.getJobDataMap().put(JOB_EXECUTOR_DATA_MAP_KEY, jobExecutor);
        return result;
    }

    /** 注册启动信息 **/
    private void registerStartUpInfo() {
        JobRegistry.getInstance().registerRegistryCenter(jobConfig.getJobName(), regCenter);
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        JobRegistry.getInstance().setCurrentShardingTotalCount(jobConfig.getJobName(), jobConfig.getShardingTotalCount());
        setUpFacade.registerStartUpInfo(!jobConfig.isDisabled());
    }

    /**
     * Shutdown job.
     */
    public void shutdown() {
        schedulerFacade.shutdownInstance();
        jobExecutor.shutdown();
    }

    public CoordinatorRegistryCenter getRegistryCenter() {
        return regCenter;
    }

    public JobConfiguration getJobConfig() {
        return jobConfig;
    }

    public JobScheduleController getJobSchedulerController() {
        return jobScheduleController;
    }
}
