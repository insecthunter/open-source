package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.configuration.pojo.JobConfigurationPOJO;
import com.simba.elasticjob.internal.config.ConfigurationNode;
import com.simba.elasticjob.internal.instance.InstanceNode;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.server.ServerNode;
import com.simba.elasticjob.internal.yaml.YamlEngine;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 分片监听器管理器
 * @Author yuanjx3
 * @Date 2021/1/19 12:46
 * @Version V1.0
 **/
public class ShardingListenerManager extends AbstractListenerManager {
    private final String jobName;
    private final ConfigurationNode configNode;
    private final InstanceNode instanceNode;
    private final ServerNode serverNode;
    private final ShardingService shardingService;

    public ShardingListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter, jobName);
        this.jobName = jobName;
        configNode = new ConfigurationNode(jobName);
        instanceNode = new InstanceNode(jobName);
        serverNode = new ServerNode(jobName);
        shardingService = new ShardingService(registryCenter, jobName);
    }
    @Override
    public void start() {
        addDataListener(new ShardingTotalCountChangedJobListener());
        addDataListener(new ListenServersChangedJobListener());
    }

    class ShardingTotalCountChangedJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            log.debug("data 发生变化，重新进行job sharding~~~~~~~~~~");
            if (configNode.isConfigPath(path) && 0!= JobRegistry.getInstance().getCurrentShardingTotalCount(jobName)){
                int newShardingTotalCount = YamlEngine.unmarshal(data, JobConfigurationPOJO.class).toJobConfiguration().getShardingTotalCount();
                if (newShardingTotalCount != JobRegistry.getInstance().getCurrentShardingTotalCount(jobName)){
                    shardingService.setReshardingFlag();
                    JobRegistry.getInstance().setCurrentShardingTotalCount(jobName, newShardingTotalCount);
                }
            }
        }
    }

    class ListenServersChangedJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (!JobRegistry.getInstance().isShutdown(jobName) && (isInstanceChange(eventType, path) || isServerChange(path))){
                shardingService.setReshardingFlag();
            }
        }

        private boolean isInstanceChange(Type eventType, String path){
            return instanceNode.isInstancesPath(path) && Type.NODE_CHANGED!=eventType;
        }
        private boolean isServerChange(String path){
            return serverNode.isServerPath(path);
        }
    }
}
