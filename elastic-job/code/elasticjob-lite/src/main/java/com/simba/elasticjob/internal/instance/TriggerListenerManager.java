package com.simba.elasticjob.internal.instance;

import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

/**
 * @Description 作业触发侦听器管理器（Job trigger listener manager）
 * @Author yuanjx3
 * @Date 2021/1/19 16:05
 * @Version V1.0
 **/
public final class TriggerListenerManager extends AbstractListenerManager {
    private final String jobName;
    private final InstanceNode instanceNode;
    private final InstanceService instanceService;

    public TriggerListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        super(registryCenter, jobName);
        this.jobName = jobName;
        instanceNode = new InstanceNode(jobName);
        instanceService = new InstanceService(registryCenter, jobName);
    }

    @Override
    public void start() {
        addDataListener(new JobTriggerStatusJobListener());
    }

    class JobTriggerStatusJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (!InstanceOperation.TRIGGER.name().equals(data)|| !instanceNode.isLocalInstancesPath(path) || Type.NODE_CHANGED != eventType){
                return;
            }
            instanceService.clearTriggerFlag();
            if (!JobRegistry.getInstance().isShutdown(jobName) && !JobRegistry.getInstance().isJobRunning(jobName)){
                //TODO  目前不能在作业运行时触发，以后会更改为堆叠触发器。
                JobRegistry.getInstance().getJobScheduleController(jobName).triggerJob();
            }
        }
    }
}
