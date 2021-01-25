package com.simba.elasticjob.internal.election;

import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.BlockUtils;
import com.simba.elasticjob.internal.storage.LeaderExecutionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description  leader 选举服务类
 * @Author yuanjx3
 * @Date 2021/1/15 15:45
 * @Version V1.0
 **/
public class LeaderService {
    public Logger log = LoggerFactory.getLogger(getClass());
    private String jobName;
    private ServerService serverService;
    private JobNodeStorage jobNodeStorage;

    public LeaderService(CoordinatorRegistryCenter registryCenter, String jobName){
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        serverService = new ServerService(registryCenter, jobName);
    }
    
    /** 功能描述:   leader 服务器选举
    * @return: void
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/15 20:41
    */
    public void electLeader(){
        log.debug(">>>>>>>>>>>>>>>>>>>>开始选新leader啦<<<<<<<<<<<<<<<<<<<<");
        jobNodeStorage.executeInLeader(LeaderNode.LATCH, new LeaderElectionExecutionCallback());
        log.debug(">>>>>>>>>>>>>>>>>>>>新leader选举结束啦<<<<<<<<<<<<<<<<<<<<");
    }
    
    /** 功能描述: 如果正在进行leader选举，该方法会阻塞等待，直到leader选举成功
    * @Author: yuanjx3
    * @Date: 2021/1/17 13:10
    */
    public boolean isLeaderUntilBlock(){
        while (!hasLeader() && serverService.hasAvailableServers()){
            log.debug("~~~~~~正在进行leader选举，再等待 "+ 100 +" ms 看看");
            BlockUtils.waitingShorTime();
            if (!JobRegistry.getInstance().isShutdown(jobName) && serverService.isAvailableServer(JobRegistry.getInstance().getJobInstance(jobName).getIp())){
                electLeader();
            }
        }
        return isLeader();
    }
    
    /** 功能描述:   判断当前服务器是否为leader节点
    * @Author: yuanjx3
    * @Date: 2021/1/17 13:40
    */
    public boolean isLeader() {
        return !JobRegistry.getInstance().isShutdown(jobName) &&
                JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId().equals(jobNodeStorage.getJobNodeData(LeaderNode.INSTANCE));
    }

    /** 功能描述: 判断 leader 实例节点是否存在，存在表示 leader 已经选举成功
    * @Author: yuanjx3
    * @Date: 2021/1/17 12:52
    */
    public boolean hasLeader(){
        return jobNodeStorage.isJobNodeExisted(LeaderNode.INSTANCE);
    }

    /** 功能描述: 删除leader节点
    * @Author: yuanjx3
    * @Date: 2021/1/17 13:44
    */
    public void removeLeader(){
        log.debug("移除leader："+ LeaderNode.INSTANCE);
        jobNodeStorage.removeJobNodeIfExisted(LeaderNode.INSTANCE);
    }
    class LeaderElectionExecutionCallback implements LeaderExecutionCallback {

        @Override
        public void execute() {
            // 如果leader还未选举出来
            if (!hasLeader()){
                log.debug("leader还未产生，去抢先创建leader节点，path："+ LeaderNode.INSTANCE + ", jobInstanceId: " + JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
                // 就去创建临时节点：{path:"leader/election/instance", value: jobInstanceId}
                jobNodeStorage.fillEphemeralJobNode(LeaderNode.INSTANCE, JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
            }
        }
    }
}
