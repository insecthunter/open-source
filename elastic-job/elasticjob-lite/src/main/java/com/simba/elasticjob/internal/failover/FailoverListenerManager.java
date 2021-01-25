package com.simba.elasticjob.internal.failover;

import com.simba.elasticjob.configuration.pojo.JobConfigurationPOJO;
import com.simba.elasticjob.internal.config.ConfigurationNode;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.instance.InstanceNode;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.internal.yaml.YamlEngine;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * @Description 故障转移监听器管理器
 * @Author yuanjx3
 * @Date 2021/1/19 13:44
 * @Version V1.0
 **/
public class FailoverListenerManager extends AbstractListenerManager {
    private final String jobName;
    private final ConfigurationService configService;
    private final ShardingService shardingService;
    private final FailoverService failoverService;
    private final ConfigurationNode configNode;
    private final InstanceNode instanceNode;

    public FailoverListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter, jobName);
        this.jobName = jobName;
        configService = new ConfigurationService(registryCenter, jobName);
        shardingService = new ShardingService(registryCenter, jobName);
        failoverService = new FailoverService(registryCenter, jobName);
        configNode = new ConfigurationNode(jobName);
        instanceNode = new InstanceNode(jobName);
    }

    @Override
    public void start() {
        addDataListener(new JobCrashedJobListener());
        addDataListener(new FailoverSettingsChangedJobListener());
    }

    private boolean isFailoverEnabled() {
        return configService.load(true).isFailover();
    }

    class JobCrashedJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (!JobRegistry.getInstance().isShutdown(jobName)
                    && isFailoverEnabled()
                    && Type.NODE_DELETED == eventType
                    && instanceNode.isInstancesPath(path)){
                String jobInstanceId = path.substring(instanceNode.getInstancesFullPath().length() + 1);
                if (jobInstanceId.equals(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId())){
                    return;
                }
                List<Integer> failoverItems = failoverService.getFailoverItems(jobInstanceId);
                if (!failoverItems.isEmpty()){
                    for (int each : failoverItems) {
                        failoverService.setCrashedFailoverFlag(each);
                        failoverService.failoverIfNecessary();
                    }
                }else{
                    for (int each : shardingService.getShardingItems(jobInstanceId)) {
                        failoverService.setCrashedFailoverFlag(each);
                        failoverService.failoverIfNecessary();
                    }
                }
            }
        }
    }

    class FailoverSettingsChangedJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (configNode.isConfigPath(path)
                    && Type.NODE_CHANGED == eventType
                    && !YamlEngine.unmarshal(data, JobConfigurationPOJO.class).toJobConfiguration().isFailover()) {
                failoverService.removeFailoverInfo();
            }
        }
    }
}
