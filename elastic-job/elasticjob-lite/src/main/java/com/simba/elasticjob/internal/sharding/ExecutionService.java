package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.listener.ShardingContexts;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description 作业运行类
 * @Author yuanjx3
 * @Date 2021/1/17 15:31
 * @Version V1.0
 **/
public class ExecutionService {
    private String jobName;
    private JobNodeStorage jobNodeStorage;
    private ConfigurationService configurationService;
    public ExecutionService(CoordinatorRegistryCenter registryCenter, String jobName){
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        configurationService = new ConfigurationService(registryCenter,jobName);
    }
    
    /** 功能描述: 注册作业开始执行
    * @Author: yuanjx3
    * @Date: 2021/1/17 16:44
    */
    public void registerJobBegin(ShardingContexts shardingContexts){
        JobRegistry.getInstance().setJobRunning(jobName,true);
        if (!configurationService.load(true).isMonitorExecution()){
            return;
        }
        for (int each : shardingContexts.getShardingItemParameters().keySet()){
            jobNodeStorage.fillEphemeralJobNode(ShardingNode.getRunningNode(each), "");
        }
    }

    /** 功能描述: 注册作业结束执行
     * @Author: yuanjx3
     * @Date: 2021/1/17 16:44
     */
    public void registerJobCompleted(ShardingContexts shardingContexts){
        JobRegistry.getInstance().setJobRunning(jobName,false);
        if (!configurationService.load(true).isMonitorExecution()){
            return;
        }
        for (int each : shardingContexts.getShardingItemParameters().keySet()){
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
        }
    }
    
    /** 功能描述: 清理所有运行信息
    * @Author: yuanjx3
    * @Date: 2021/1/17 16:49
    */
    public void clearAllRunningInfo(){
        clearRunningInfo(getAllItems());
    }
    
    /** 功能描述:  清理运行数据
    * @Author: yuanjx3
    * @Date: 2021/1/17 16:50
    */
    public void clearRunningInfo(List<Integer> items){
        for (int each:items){
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getRunningNode(each));
        }
    }

    /** 功能描述:  判断是否是否有任务分片在执行
     * @Author: yuanjx3
     * @Date: 2021/1/17 16:50
     */
    public boolean hashRunningItems(Collection<Integer> items){
        JobConfiguration jobConfiguration = configurationService.load(true);
        if (!jobConfiguration.isMonitorExecution()){
            return false;
        }
        for (int each:items){
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getRunningNode(each))){
                return true;
            }
        }
        return false;
    }

    public boolean hashRunningItems(){
        return hashRunningItems(getAllItems());
    }

    /** 功能描述:  获取所有分片项编号集合
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:20
    */
    private List<Integer> getAllItems() {
        int shardingTotalCount = configurationService.load(true).getShardingTotalCount();
        List<Integer> result = new ArrayList<>(shardingTotalCount);
        for (int i=0;i<shardingTotalCount;i++){
            result.add(i);
        }
        return result;
    }
    
    /** 功能描述: 如果有作业分片依然在执行中，设置未触发标识符
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:22
    */
    public boolean misfireIfHasRunningItems(Collection<Integer> items){
        if (!hashRunningItems(items)){
            return false;
        }
        setMisfire(items);
        return true;
    }

    public void setMisfire(Collection<Integer> items) {
        for (int each:items){
            jobNodeStorage.createJobNodeIfNeeded(ShardingNode.getMisfireNode(each));
        }
    }
    
    /** 功能描述: 获取未触发执行的分片项集合
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:52
    */
    public List<Integer> getMisfiredJobItems(Collection<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each:items){
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getMisfireNode(each))){
                result.add(each);
            }
        }
        return result;
    }

    /** 功能描述: 清理未触发作业分片项
     * @Author: yuanjx3
     * @Date: 2021/1/17 17:52
     */
    public void clearMisfire(Collection<Integer> items) {
        for (int each:items){
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getMisfireNode(each));
        }
    }

    /** 功能描述: 获取不可用作业分片项
     * @Author: yuanjx3
     * @Date: 2021/1/17 17:52
     */
    public List<Integer> getDisabledItems(List<Integer> items) {
        List<Integer> result = new ArrayList<>(items.size());
        for (int each:items){
            if (jobNodeStorage.isJobNodeExisted(ShardingNode.getDisabledNode(each))){
                result.add(each);
            }
        }
        return result;
    }
}
