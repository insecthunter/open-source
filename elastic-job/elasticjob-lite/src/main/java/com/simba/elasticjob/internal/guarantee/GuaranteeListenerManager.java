package com.simba.elasticjob.internal.guarantee;

import com.simba.elasticjob.internal.listener.AbstractDistributeOnceElasticJobListener;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.listener.ElasticJobListener;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * @Description Guarantee listener manager
 * @Author yuanjx3
 * @Date 2021/1/19 17:06
 * @Version V1.0
 **/
public final class GuaranteeListenerManager extends AbstractListenerManager {
    private final GuaranteeNode guaranteeNode;
    private final Collection<ElasticJobListener> elasticJobListeners;

    public GuaranteeListenerManager(CoordinatorRegistryCenter registryCenter, String jobName, Collection<ElasticJobListener> elasticJobListeners){
        super(registryCenter,jobName);
        this.guaranteeNode = new GuaranteeNode(jobName);
        this.elasticJobListeners = elasticJobListeners;
    }

    @Override
    public void start() {
        addDataListener(new StartedNodeRemovedJobListener());
        addDataListener(new CompletedNodeRemovedJobListener());
    }

    class StartedNodeRemovedJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (Type.NODE_DELETED == eventType && guaranteeNode.isStartedRootNode(path)){
                for (ElasticJobListener each : elasticJobListeners){
                    if (each instanceof AbstractDistributeOnceElasticJobListener){
                        ((AbstractDistributeOnceElasticJobListener) each).notifyWaitingTaskStart();
                    }
                }
            }
        }
    }

    class CompletedNodeRemovedJobListener extends AbstractJobListener{

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            if (Type.NODE_DELETED == eventType && guaranteeNode.isCompletedRootNode(path)){
                for (ElasticJobListener each : elasticJobListeners){
                    if (each instanceof AbstractDistributeOnceElasticJobListener){
                        ((AbstractDistributeOnceElasticJobListener) each).notifyWaitingTaskStart();
                    }
                }
            }
        }
    }
}
