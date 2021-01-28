package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.handler.sharding.JobShardingStrategy;
import com.simba.elasticjob.handler.sharding.JobShardingStrategyFactory;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.election.LeaderService;
import com.simba.elasticjob.internal.instance.InstanceNode;
import com.simba.elasticjob.internal.instance.InstanceService;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.internal.storage.JobNodePath;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.internal.storage.TransactionExecutionCallback;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.BlockUtils;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;

import java.util.*;

/**
 * @Description 分片服务类
 * @Author yuanjx3
 * @Date 2021/1/17 15:28
 * @Version V1.0
 **/
public final class ShardingService {
    private String jobName;
    private JobNodeStorage jobNodeStorage;
    private LeaderService leaderService;
    private ConfigurationService configurationService;
    private InstanceService instanceService;
    private ServerService serverService;
    private ExecutionService executionService;
    private JobNodePath jobNodePath;

    public ShardingService(CoordinatorRegistryCenter registryCenter,String jobName){
        this.jobName  = jobName;
        jobNodeStorage = new JobNodeStorage(registryCenter,jobName);
        leaderService = new LeaderService(registryCenter,jobName);
        configurationService = new ConfigurationService(registryCenter,jobName);
        instanceService = new InstanceService(registryCenter, jobName);
        serverService = new ServerService(registryCenter, jobName);
        executionService = new ExecutionService(registryCenter, jobName);
        jobNodePath = new JobNodePath(jobName);
    }
    
    /** 功能描述: 设置重新分片标识
    * @Author: yuanjx3
    * @Date: 2021/1/17 18:03
    */
    public void setReshardingFlag(){
        if (!leaderService.isLeaderUntilBlock()){
            return;
        }
        //创建leader/sharding/necessary节点
        jobNodeStorage.createJobNodeIfNeeded(ShardingNode.NECESSARY);
    }

    /** 功能描述: 判断是否需要重写分片(leader/sharding/necessary节点存在，表示需要进行重新分片)
     * @Author: yuanjx3
     * @Date: 2021/1/17 18:03
     */
    public boolean isNeedSharding(){
        return jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY);
    }

    /** 功能描述: 进行分片
     * @Author: yuanjx3
     * @Date: 2021/1/17 18:03
     */
    public void shardingIfNecessary(){
        // 获取可用的作业服务器实例
        List<JobInstance> availableJobInstances = instanceService.getAvailableJobInstances();
        if (!isNeedSharding()||availableJobInstances.isEmpty()){
            return;
        }
        if (!leaderService.isLeaderUntilBlock()){
            blockUntilShardingCompleted();
            return;
        }
        // 等待其他的分片操作执行完毕
        waitingOtherShardingItemCompleted();
        // 获取作业配置
        JobConfiguration jobConfig = configurationService.load(false);
        // 得到作业分片总数
        int shardingTotalCount = jobConfig.getShardingTotalCount();
        System.out.println(">>>>>>>>>>>>Job "+ jobName+"分片 ---开始");
        // 创建临时节点“leader/sharding/processing”，标识leader节点正在进行分片操作
        jobNodeStorage.fillEphemeralJobNode(ShardingNode.PROCESSING, "");
        // 根据新的分片数，重新进行作业分片
        resetShardingInfo(shardingTotalCount);
        // 根据作业配置的分片策略类型，从分片策略工厂类中获取对应的分片策略
        JobShardingStrategy jobShardingStrategy = JobShardingStrategyFactory.getStrategy(jobConfig.getJobShardingStrategyType());
        // 在事务中执行作业分片
        jobNodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(jobShardingStrategy.sharding(availableJobInstances,jobName,shardingTotalCount)));
        System.out.println("》》》》》》》》Job "+ jobName +" 分片完成~~~~~~");
    }

    private void blockUntilShardingCompleted() {
        while (!leaderService.isLeaderUntilBlock()
                && (jobNodeStorage.isJobNodeExisted(ShardingNode.NECESSARY)
                || jobNodeStorage.isJobNodeExisted(ShardingNode.PROCESSING))){
            System.out.println("job +"+ jobName + "在此等待一段时间，直到分片操作结束");
            BlockUtils.waitingShorTime();
        }
    }

    private void waitingOtherShardingItemCompleted() {
        while (executionService.hashRunningItems()){
            System.out.println("job +"+ jobName + "在此等待一段时间，直到其他的作业分片执行结束");
            BlockUtils.waitingShorTime();
        }
    }
    
    /** 功能描述:  重新进行作业分片
    * @Author: yuanjx3
    * @Date: 2021/1/18 15:23
    */
    private void resetShardingInfo(int shardingTotalCount){
        /** 先删除原来的节点，然后创建新节点 **/
        for (int i=0;i<shardingTotalCount;i++){
            jobNodeStorage.removeJobNodeIfExisted(ShardingNode.getInstanceNodePath(i));
            jobNodeStorage.createJobNodeIfNeeded(ShardingNode.ROOT + "/" + i);
        }
        int actualShardingTotalCount = jobNodeStorage.getChildrenKeys(ShardingNode.ROOT).size();
        /** 原来的分片数大于现在的分片数时，将原来超出的分片节点统统删除 **/
        if (actualShardingTotalCount>shardingTotalCount){
            for (int i=shardingTotalCount;i<actualShardingTotalCount;i++){
                jobNodeStorage.removeJobNodeIfExisted(ShardingNode.ROOT + "/" + i);
            }
        }
    }
    
    /** 功能描述: 获取作业分片集合
    * @Author: yuanjx3
    * @Date: 2021/1/18 15:27
    */
    public List<Integer> getShardingItems(String jobInstanceId){
        JobInstance jobInstance = new JobInstance(jobInstanceId);
        // 先判断作业运行的服务器是否可用，如不不可用直接返回空集合
        if (!serverService.isAvailableServer(jobInstance.getIp())){
            return Collections.emptyList();
        }
        List<Integer> result = new LinkedList<>();
        // 加载作业的配置信息，获取作业分片数
        int shardingTotalCount = configurationService.load(true).getShardingTotalCount();
        // 遍历所有分片节点，如果是jobInstanceId对应的作业的分片，则将分片编码加入到集合中
        for (int i=0;i<shardingTotalCount;i++){
            if (jobInstance.getJobInstanceId().equals(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNodePath(i)))){
                result.add(i);
            }
        }
        return result;
    }

    /** 功能描述: 从本地作业服务器获取作业分片集合
     * @Author: yuanjx3
     * @Date: 2021/1/18 15:27
     */
    public List<Integer> getLocalShardingItems(){
        if (JobRegistry.getInstance().isShutdown(jobName)
                || !serverService.isAvailableServer(JobRegistry.getInstance().getJobInstance(jobName).getIp())){
            return Collections.emptyList();
        }
        return getShardingItems(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }

    /** 功能描述: 查询分片数据是否在保存在离线的服务器上
     * @Author: yuanjx3
     * @Date: 2021/1/18 15:27
     */
    public boolean hasShardingInfoInOfflineServers(){
        List<String> onlineInstances = jobNodeStorage.getChildrenKeys(InstanceNode.ROOT);
        int shardingTotalCount = configurationService.load(true).getShardingTotalCount();
        for (int i=0;i<shardingTotalCount;i++){
            if (!onlineInstances.contains(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNodePath(i)))){
                return true;
            }
        }
        return false;
    }

    class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {
        private final Map<JobInstance, List<Integer>> shardingResults;

        public PersistShardingInfoTransactionExecutionCallback(Map<JobInstance, List<Integer>> shardingResults) {
            this.shardingResults = shardingResults;
        }

        @Override
        public List<CuratorOp> createCuratorOperators(TransactionOp transactionOp) throws Exception {
            List<CuratorOp> result = new LinkedList<>();
            for (Map.Entry<JobInstance,List<Integer>> entry:shardingResults.entrySet()){
                for (int shardingItem:entry.getValue()){
                    result.add(transactionOp.create().forPath(jobNodePath.getFullPath(ShardingNode.getInstanceNodePath(shardingItem)),entry.getKey().getJobInstanceId().getBytes()));
                }
            }
            result.add(transactionOp.delete().forPath(jobNodePath.getFullPath(ShardingNode.NECESSARY)));
            result.add(transactionOp.delete().forPath(jobNodePath.getFullPath(ShardingNode.PROCESSING)));
            return result;
        }
    }
}
