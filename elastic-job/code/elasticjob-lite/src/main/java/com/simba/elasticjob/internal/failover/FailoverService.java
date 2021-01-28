package com.simba.elasticjob.internal.failover;

import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.schedule.JobScheduleController;
import com.simba.elasticjob.internal.sharding.ShardingNode;
import com.simba.elasticjob.internal.sharding.ShardingService;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.internal.storage.LeaderExecutionCallback;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Description 故障转移服务类（容错）
 * @Author yuanjx3
 * @Date 2021/1/19 13:51
 * @Version V1.0
 **/
public class FailoverService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String jobName;
    private final JobNodeStorage jobNodeStorage;
    private final ShardingService shardingService;

    public FailoverService(CoordinatorRegistryCenter registryCenter, String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        shardingService = new ShardingService(registryCenter, jobName);
    }

    /** 功能描述: 设置崩溃故障转移标志
    * @param: [item: 崩溃的作业项目]
    * @Author: yuanjx3
    * @Date: 2021/1/19 13:53
    */
    public void setCrashedFailoverFlag(int item){
        if (!isFailoverAssigned(item)){
            jobNodeStorage.createJobNodeIfNeeded(FailoverNode.getItemsNode(item));
        }
    }

    private boolean isFailoverAssigned(Integer item){
        return jobNodeStorage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(item));
    }

    public void failoverIfNecessary() {
        if (needFailover()) {
            jobNodeStorage.executeInLeader(FailoverNode.LATCH, new FailoverLeaderExecutionCallback());
        }
    }

    /** 功能描述:   判断是否进行故障转移
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:01
    */
    private boolean needFailover(){
        // 如果故障转移节点存在，并且作业节点已经停止运行，则需要进行故障转移，返回true
        return jobNodeStorage.isJobNodeExisted(FailoverNode.ITEMS_ROOT)
                && !jobNodeStorage.getChildrenKeys(FailoverNode.ITEMS_ROOT).isEmpty()
                && !JobRegistry.getInstance().isJobRunning(jobName);
    }
    
    /** 功能描述: 当故障转移执行完成时，更新分片项状态
    * @param: [items: 已完成故障转移执行的分片项]
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:04
    */
    public void updateFailoverComplete(Collection<Integer> items){
        for (int each:items){
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(each));
        }
    }

    /** 功能描述: 获取故障转移分片项
    *
    * @param: [jobInstanceId：作业实例ID]
    * @return: java.util.List<java.lang.Integer>
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:10
    */
    public List<Integer> getFailoverItems(String jobInstanceId) {
        List<String> items = jobNodeStorage.getChildrenKeys(ShardingNode.ROOT);
        List<Integer> result = new ArrayList<>(items.size());
        for (String each : items){
            int item = Integer.parseInt(each);
            String node = FailoverNode.getExecutionFailoverNode(item);
            if (jobNodeStorage.isJobNodeExisted(node) && jobInstanceId.equals(jobNodeStorage.getJobNodeDataDirectly(node))){
                result.add(item);
            }
        }
        Collections.sort(result);
        return result;
    }

    /** 功能描述: 获取在本地主机上执行的故障转移项。
    * @return: [List<Integer>: 在本地主机上执行的故障转移项目]
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:15
    */
    public List<Integer> getLocalFailoverItems() {
        if (JobRegistry.getInstance().isShutdown(jobName)){
            return Collections.emptyList();
        }
        return getFailoverItems(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }

    /** 功能描述: 获取在本地主机上崩溃的故障转移项。
     * @return: [List<Integer>: 在本地主机上崩溃的故障转移项目]
     * @Author: yuanjx3
     * @Date: 2021/1/19 14:15
     */
    public List<Integer> getLocalTakeOffItems() {
        List<Integer> shardingItems = shardingService.getLocalShardingItems();
        List<Integer> result = new ArrayList<>(shardingItems.size());
        for (int each : shardingItems){
            if (jobNodeStorage.isJobNodeExisted(FailoverNode.getExecutionFailoverNode(each))){
                result.add(each);
            }
        }
        return result;
    }

    /** 功能描述:  删除故障转移信息
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:25
    */
    public void removeFailoverInfo(){
        for (String each : jobNodeStorage.getChildrenKeys(ShardingNode.ROOT)){
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getExecutionFailoverNode(Integer.parseInt(each)));
        }
    }

    class FailoverLeaderExecutionCallback implements LeaderExecutionCallback {

        @Override
        public void execute() {
            if (JobRegistry.getInstance().isShutdown(jobName) || !needFailover()){
                return;
            }
            int crashedItem = Integer.parseInt(jobNodeStorage.getChildrenKeys(FailoverNode.ITEMS_ROOT).get(0));
            logger.debug("Failover job '{}' begin, crashed item '{}'", jobName,crashedItem);
            jobNodeStorage.fillEphemeralJobNode(FailoverNode.getExecutionFailoverNode(crashedItem), JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
            jobNodeStorage.removeJobNodeIfExisted(FailoverNode.getItemsNode(crashedItem));
            //TODO 使用executor进行统一调度，而不是使用triggerJob
            JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
            if (null != jobScheduleController){
                jobScheduleController.triggerJob();
            }

        }
    }
}
