package com.simba.elasticjob.internal.storage;

/**
 * @Description 任务节点路径类
 * @Author yuanjx3
 * @Date 2021/1/14 20:10
 * @Version V1.0
 **/
public class JobNodePath {
    private static final String LEADER_HOST_NODE = "leader/election/instance";    //leader节点信息存储路径
    private static final String CONFIG_NODE = "config";
    private static final String SERVERS_NODE = "servers";
    private static final String INSTANCES_NODE = "instances";
    private static final String SHARDING_NODE = "sharding";
    private final String jobName;

    public JobNodePath(String jobName) {
        this.jobName = jobName;
    }

    public String getFullPath(String node){
        return String.format("/%s/%s", jobName, node);
    }

    /** 获取 leader 主机节点的路径 **/
    public String getLeaderHostNodePath(){
        return String.format("/%s/%s", jobName, LEADER_HOST_NODE);
    }

    public String getServersNodePath(){
        return String.format("/%s/%s", jobName, SERVERS_NODE);
    }

    public String getServerNodePath(String serverIp){
        return String.format("/%s/%s", getServersNodePath(), serverIp);
    }

    public String getInstancesNodePath(){
        return String.format("/%s/%s", jobName, INSTANCES_NODE);
    }

    public String getConfigNodePath(){
        return String.format("/%s/%s", jobName, CONFIG_NODE);
    }

    public String getInstanceNodePath(String instanceId){
        return String.format("/%s/%s", getInstancesNodePath(), instanceId);
    }

    public String getShardingNodePath(){
        return String.format("/%s/%s", jobName, SHARDING_NODE);
    }

    /** 功能描述: 获取分片节点路径
    * @param: [item：总分片数量, nodeName：节点名称]
    * @return: java.lang.String
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/14 20:23
    */
    public String getShardingNodePath(String item, String nodeName){
        return String.format("/%s/%s/%s", getShardingNodePath(), item, nodeName);
    }
}
