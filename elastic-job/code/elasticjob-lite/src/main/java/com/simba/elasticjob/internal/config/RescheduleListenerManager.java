package com.simba.elasticjob.internal.config;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.configuration.pojo.JobConfigurationPOJO;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.yaml.YamlEngine;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.StringUtils;

/**
 * @Description 重新调度监听管理器（Reschedule listener manager）
 * @Author yuanjx3
 * @Date 2021/1/19 16:48
 * @Version V1.0
 **/
public final class RescheduleListenerManager extends AbstractListenerManager {
    private final ConfigurationNode configNode;
    private final String jobName;

    public RescheduleListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter, jobName);
        this.jobName = jobName;
        configNode = new ConfigurationNode(jobName);
    }

    @Override
    public void start() {
        addDataListener(new CronSettingAndJobEventChangedJobListener());
    }

    class CronSettingAndJobEventChangedJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (configNode.isConfigPath(path) && Type.NODE_CHANGED==eventType && !JobRegistry.getInstance().isShutdown(jobName)){
                JobConfiguration jobConfiguration = YamlEngine.unmarshal(data, JobConfigurationPOJO.class).toJobConfiguration();
                if (StringUtils.isEmpty(jobConfiguration.getCron())){
                    // cron表达式为空，则只调度执行作业一次
                    JobRegistry.getInstance().getJobScheduleController(jobName).reshedulerJob();
                }else {
                    // cron表达式不为空，则按照cron表达式进行周期性调度执行
                    JobRegistry.getInstance().getJobScheduleController(jobName).reshedulerJob(jobConfiguration.getCron());
                }
            }
        }
    }
}
