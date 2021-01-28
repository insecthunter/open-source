package com.simba.elasticjob.internal.election;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.internal.listener.AbstractJobListener;
import com.simba.elasticjob.internal.listener.AbstractListenerManager;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.server.ServerNode;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.internal.server.ServerStatus;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.Objects;

/**
 * @Description  选举监听器管理类
 * @Author yuanjx3
 * @Date 2021/1/19 10:22
 * @Version V1.0
 **/
public final class ElectionListenerManager extends AbstractListenerManager {
    private final String jobName;
    private final LeaderNode leaderNode;
    private final ServerNode serverNode;
    private final LeaderService leaderService;
    private final ServerService serverService;

    public ElectionListenerManager(CoordinatorRegistryCenter registryCenter,String jobName){
        super(registryCenter,jobName);
        this.jobName = jobName;
        leaderNode = new LeaderNode(jobName);
        serverNode = new ServerNode(jobName);
        leaderService = new LeaderService(registryCenter, jobName);
        serverService = new ServerService(registryCenter, jobName);
    }

    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
        addDataListener(new LeaderAbdicationJobListener());
    }
    
    /** 功能描述: leader选举作业监听器（如果节点数据发生变化，则执行新一轮的leader选举）
    * @Author: yuanjx3
    * @Date: 2021/1/19 10:46
    */
    class LeaderElectionJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            // 如果注册中心未关闭，并且主动选举或被动选举条件成了，则执行leader选举
            if (!JobRegistry.getInstance().isShutdown(jobName) && (isActiveElection(path, data) || isPassiveElection(path, eventType))){
                log.debug("leader 节点发生变化，开始新一轮的leader选举~~~~~~~~~~");
                leaderService.electLeader();
            }
        }

        // 是否是主动进行leader选举
        private boolean isActiveElection(String path, String data){
            // 如果当前没有 leader 并且本地服务器是可用状态，则返回true，表示进行主动选举
            boolean isActive = !leaderService.hasLeader() && isLocalServerEnabled(path, data);
            log.debug("is Active Election,path: " + path + ",data: "+ data, ",isActive result: " + isActive);
            return isActive;
        }

        // 是否是被动进行leader选举
        private boolean isPassiveElection(String path, Type eventType){
            JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
            // 如果作业实例不为空，同时leader节点宕机了，并且作业实例的ip 节点服务器可用，则返回true，表示进行被动leader选举
            boolean isPassive = !Objects.isNull(jobInstance) && isLeaderCrashed(path, eventType) && serverService.isAvailableServer(jobInstance.getIp());
            log.debug("is Passive Election,path: " + path + ",eventType: "+ eventType, ",isPassive result: " + isPassive);
            return isPassive;
        }

        // 是否leader服务器宕机了
        private boolean isLeaderCrashed(String path, Type eventType){
            boolean isLeaderCrashed = leaderNode.isLeaderInstancePath(path) && Type.NODE_DELETED == eventType;
            log.debug("is Leader Crashed: " + path + ",eventType: "+ eventType, ",isLeaderCrashed result: " + isLeaderCrashed);
            return isLeaderCrashed;
        }

        // 是否本地服务器可用
        private boolean isLocalServerEnabled(String path, String data){
            boolean isLocalServerEnabled = serverNode.isLocalServerPath(path) && !ServerStatus.DISABLED.name().equals(data);
            log.debug("is Local Server Enabled: " + path + ",data: "+ data, ",isLocalServerEnabled result: " + isLocalServerEnabled);
            return isLocalServerEnabled;
        }
    }

    /** 功能描述: leader废弃作业监听器
     * @Author: yuanjx3
     * @Date: 2021/1/19 10:46
     */
    class LeaderAbdicationJobListener extends AbstractJobListener {

        @Override
        protected void dataChanged(String path, Type eventType, String data) {
            // 如果当前服务器是leader节点，并且本地服务器被禁用了，则移除leader节点
            if (leaderService.isLeader() && isLocalServerDisabled(path, data)) {
                log.debug("当前leader节点服务器不可用，移除leader");
                leaderService.removeLeader();
            }
        }

        //判断本地服务器是否被禁用了
        private boolean isLocalServerDisabled(String path, String data){
            return serverNode.isLocalServerPath(path) && ServerStatus.DISABLED.name().equals(data);
        }
    }
}
