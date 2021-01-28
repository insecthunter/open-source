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
            //如果作业服务器节点停机、失败重试标识为true、事件类型为节点删除、并且是path是job工作服务器实例的root节点路径,
            if (!JobRegistry.getInstance().isShutdown(jobName)
                    && isFailoverEnabled()
                    && Type.NODE_DELETED == eventType
                    && instanceNode.isInstancesPath(path)){
                /**
                 * 截取作业分片服务器实例ID：
                 * 比如：path 为 “/my-job/MyJob/instances/192.168.0.179@-@17988”，
                 *       instanceNode.getInstancesFullPath() = “/my-job/MyJob/instances/”，
                 *       截取之后， jobInstanceId = 192.168.0.179@-@17988，结构为  ip + "@-@" + port
                **/
                String jobInstanceId = path.substring(instanceNode.getInstancesFullPath().length() + 1);
                //这里判断如果从注册中心中获取的jobInstanceId与崩溃的jobInstanceId相同，则返回
                if (jobInstanceId.equals(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId())){
                    return;
                }

                //进行故障转移
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
