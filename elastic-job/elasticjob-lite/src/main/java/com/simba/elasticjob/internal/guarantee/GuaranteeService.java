package com.simba.elasticjob.internal.guarantee;

import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * @Description 单次执行服务
 * @Author yuanjx3
 * @Date 2021/1/19 17:41
 * @Version V1.0
 **/
public final class GuaranteeService {
    private final JobNodeStorage jobNodeStorage;
    private final ConfigurationService configService;

    public GuaranteeService(CoordinatorRegistryCenter registryCenter, String jobName){
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        configService = new ConfigurationService(registryCenter, jobName);
    }

    /** 功能描述: 注册启动开始
    * @param: [shardingItems： to be registered sharding items]
    * @return: void
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/19 17:43
    */
    public void registerStart(Collection<Integer> shardingItems){
        for (int each : shardingItems){
            // 创建 "/started/each"节点，标识作业分片已启动
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getStartedNode(each));
        }
    }

    /** 功能描述: 判断当前分片项是否全部注册启动成功
     * @param: [shardingItems： current sharding items]
     * @return: boolean
     * @version: 1.0.0
     * @Author: yuanjx3
     * @Date: 2021/1/19 17:43
     */
    public boolean isRegisterStartSuccess(Collection<Integer> shardingItems){
        for (int each : shardingItems){
            if (!jobNodeStorage.isJobNodeExisted(GuaranteeNode.getStartedNode(each))){
                return false;
            }
        }
        return true;
    }

    /** 功能描述: 判断job的分片项是否全部启动。
     * @param: [shardingItems： current sharding items]
     * @return: boolean
     * @version: 1.0.0
     * @Author: yuanjx3
     * @Date: 2021/1/19 17:43
     */
    public boolean isAllStarted(){
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.STARTED_ROOT)
                && configService.load(false).getShardingTotalCount() == jobNodeStorage.getChildrenKeys(GuaranteeNode.STARTED_ROOT).size();
    }
    
    /** 功能描述:   清除所有已启动的作业的信息
    * @Author: yuanjx3
    * @Date: 2021/1/19 17:51
    */
    public void clearAllStartedInfo(){
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.STARTED_ROOT);
    }

    /** 功能描述:   注册完成
     * @Author: yuanjx3
     * @Date: 2021/1/19 17:51
     */
    public void registerComplete(Collection<Integer> shardingItems){
        for (int each : shardingItems){
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getCompletedNode(each));
        }
    }

    /** 功能描述: 判断分片项是否全部注册成功
     *
     * @param: [shardingItems： current sharding items]
     * @return: boolean
     * @version: 1.0.0
     * @Author: yuanjx3
     * @Date: 2021/1/19 17:43
     */
    public boolean isRegisterCompleteSuccess(Collection<Integer> shardingItems){
        for (int each : shardingItems){
            if (!jobNodeStorage.isJobNodeExisted(GuaranteeNode.getCompletedNode(each))){
                return false;
            }
        }
        return true;
    }

    /**
     * Judge whether job's sharding items are all completed.
     *
     * @return job's sharding items are all completed or not
     */
    public boolean isAllCompleted() {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.COMPLETED_ROOT)
                && configService.load(false).getShardingTotalCount() <= jobNodeStorage.getChildrenKeys(GuaranteeNode.COMPLETED_ROOT).size();
    }

    /**
     * Clear all completed job's info.
     */
    public void clearAllCompletedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.COMPLETED_ROOT);
    }
}
