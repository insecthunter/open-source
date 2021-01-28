package com.simba.elasticjob.internal.instance;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description 作业实例服务类
 * @Author yuanjx3
 * @Date 2021/1/17 14:07
 * @Version V1.0
 **/
public class InstanceService {
    private JobNodeStorage jobNodeStorage;
    private InstanceNode instanceNode;
    private ServerService serverService;

    public InstanceService(CoordinatorRegistryCenter registryCenter, String jobName){
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        instanceNode = new InstanceNode(jobName);
        serverService = new ServerService(registryCenter, jobName);
    }

    /** 功能描述: 保存作业上线信息（在注册中心上创建一个以作业运行实例主键为key，value为空的临时节点）
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/17 14:11
    */
    public void persistOnline(){
        jobNodeStorage.fillEphemeralJobNode(instanceNode.getLocalInstancePath(), "");
    }
    
    /** 功能描述: 删除运行实例
    * @Author: yuanjx3
    * @Date: 2021/1/17 14:20
    */
    public void removeInstance(){
        jobNodeStorage.removeJobNodeIfExisted(instanceNode.getLocalInstancePath());
    }

    /** 功能描述: 清理触发标记
    * @Author: yuanjx3
    * @Date: 2021/1/17 14:22
    */
    void clearTriggerFlag(){
        jobNodeStorage.updateJobNode(instanceNode.getLocalInstancePath(), "");
    }

    /** 功能描述: 获取可用的作业运行实例列表
    * @Author: yuanjx3
    * @Date: 2021/1/17 14:23
    */
    public List<JobInstance> getAvailableJobInstances(){
        List<JobInstance> result = new LinkedList<>();
        for (String each: jobNodeStorage.getChildrenKeys(InstanceNode.ROOT)){
            JobInstance jobInstance = new JobInstance(each);
            if (serverService.isEnableServer(jobInstance.getIp())){
                result.add(new JobInstance(each));
            }
        }
        return  result;
    }

    boolean isLocalJobInstanceExisted(){
        return jobNodeStorage.isJobNodeExisted(instanceNode.getLocalInstancePath());
    }

    public void triggerAllInstances(){
        jobNodeStorage.getChildrenKeys(InstanceNode.ROOT)
                .forEach(each-> jobNodeStorage.replaceJobNode(instanceNode.getInstancePath(each),InstanceOperation.TRIGGER.name()));
    }
}
