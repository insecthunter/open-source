package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.configuration.pojo.JobConfigurationPOJO;
import com.simba.elasticjob.internal.config.ConfigurationNode;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.yaml.YamlEngine;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 监视执行监听器管理器
 * @Author yuanjx3
 * @Date 2021/1/19 15:27
 * @Version V1.0
 **/
public final class MonitorExecutionListenerManager extends AbstractListenerManager {
    private final ExecutionService executionService;
    private final ConfigurationNode configNode;

    public MonitorExecutionListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter,jobName);
        executionService = new ExecutionService(registryCenter, jobName);
        configNode = new ConfigurationNode(jobName);
    }
    @Override
    public void start() {
        addDataListener(new MonitorExecutionSettingsChangedJobListener());
    }

    class MonitorExecutionSettingsChangedJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (configNode.isConfigPath(path)
                    && Type.NODE_CHANGED==eventType
                    && !YamlEngine.unmarshal(data, JobConfigurationPOJO.class).toJobConfiguration().isMonitorExecution()) {
                        executionService.clearAllRunningInfo();
            }
        }
    }
}
