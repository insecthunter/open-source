package com.simba.elasticjob.internal.instance;

import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.schedule.SchedulerFacade;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 作业实例关闭监听器管理器
 * @Author yuanjx3
 * @Date 2021/1/19 15:35
 * @Version V1.0
 **/
public final class ShutdownListenerManager extends AbstractListenerManager {
    private final String jobName;
    private final InstanceNode instanceNode;
    private final InstanceService instanceService;
    private final SchedulerFacade schedulerFacade;

    public ShutdownListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter,jobName);
        this.jobName = jobName;
        instanceNode = new InstanceNode(jobName);
        instanceService = new InstanceService(registryCenter,jobName);
        schedulerFacade = new SchedulerFacade(registryCenter,jobName);
    }
    @Override
    public void start() {
        addDataListener(new InstanceShutdownStatusJobListener());
    }

    class InstanceShutdownStatusJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (!JobRegistry.getInstance().isShutdown(jobName)
                    && !JobRegistry.getInstance().getJobScheduleController(jobName).isPaused()
                    && isRemoveInstance(path,eventType) && !isReconnectedRegistryCenter() ){
                schedulerFacade.shutdownInstance();
            }
        }

        private boolean isRemoveInstance(String path, Type eventType){
            return instanceNode.isLocalInstancesPath(path) && Type.NODE_DELETED == eventType;
        }

        private boolean isReconnectedRegistryCenter() {
            return instanceService.isLocalJobInstanceExisted();
        }
    }
}
