package com.simba.elasticjob.internal.election;


import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description Leader path 节点类
 * @Author yuanjx3
 * @Date 2021/1/15 20:43
 * @Version V1.0
 **/
public class LeaderNode {
    public static final String ROOT = "leader";
    private static final String ELECTION_ROOT = ROOT + "/election";
    static final String INSTANCE = ELECTION_ROOT + "/instance";
    static final String LATCH = ELECTION_ROOT + "/latch";
    private final JobNodePath jobNodePath;

    LeaderNode(String jobName){
        jobNodePath = new JobNodePath(jobName);
    }

    /** 功能描述: 判断path是否是当前leader 实例节点的path
    * @Author: yuanjx3
    * @Date: 2021/1/17 12:45
    */
    boolean isLeaderInstancePath(String path){
        return jobNodePath.getFullPath(INSTANCE).equals(path);
    }

}
