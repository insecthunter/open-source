package com.simba.elasticjob.internal.server;

import com.simba.elasticjob.internal.instance.InstanceNode;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.BlockUtils;
import com.simba.elasticjob.utils.StringUtils;

import java.util.List;

/**
 * @Description  服务器服务类
 * @Author yuanjx3
 * @Date 2021/1/15 15:47
 * @Version V1.0
 **/
public class ServerService {
    private String jobName;
    private JobNodeStorage jobNodeStorage;
    private ServerNode serverNode;
    public ServerService(CoordinatorRegistryCenter registryCenter, String jobName){
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        serverNode = new ServerNode(jobName);
    }

    /** 功能描述: Persist online status of job server（保存作业服务器上线状态）
     * @param enabled enable server or not  （是否启用服务器）
     * @Author: yuanjx3
     * @Date: 2021/1/15 20:01
     */
    public void persistOnline(boolean enabled){
        if (!JobRegistry.getInstance().isJobRunning(jobName)){
            jobNodeStorage.fillJobNode(serverNode.getServerNode(JobRegistry.getInstance().getJobInstance(jobName).getIp()), enabled?ServerStatus.ENABLED.name():ServerStatus.DISABLED.name());
        }
    }
    
    /** 功能描述: 判断是否有可用的服务器
    * @Author: yuanjx3
    * @Date: 2021/1/15 20:10
    */
    public boolean hasAvailableServers(){
        List<String> servers = jobNodeStorage.getChildrenKeys(ServerNode.ROOT);
        for (String serverIp: servers){
            if(isAvailableServer(serverIp)){
                return true;
            }
        }
        return false;
    }

    public boolean isAvailableServer(String serverIp) {
        return isEnableServer(serverIp) && hasOnlineInstances(serverIp);
    }

    private boolean hasOnlineInstances(String ip) {
        for (String each : jobNodeStorage.getChildrenKeys(InstanceNode.ROOT)){
            if (each.startsWith(ip)){
                return true;
            }
        }
        return false;
    }

    /** 功能描述:   判断服务器是否可用
    * @param: [ip]  要判断是否可用的服务器的IP地址
    * @return: boolean
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/15 20:37
    */
    public boolean isEnableServer(String ip) {
        String serveStatus = jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip));
        while (StringUtils.isNullOrEmpty(serveStatus)){
            BlockUtils.waitingShorTime();
            serveStatus = jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip));
        }
        return !ServerStatus.DISABLED.name().equals(serveStatus);
    }


}
