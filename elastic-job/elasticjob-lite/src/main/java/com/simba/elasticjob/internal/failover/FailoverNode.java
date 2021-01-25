package com.simba.elasticjob.internal.failover;

import com.simba.elasticjob.internal.election.LeaderNode;
import com.simba.elasticjob.internal.sharding.ShardingNode;
import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description 故障转移节点
 * @Author yuanjx3
 * @Date 2021/1/19 14:35
 * @Version V1.0
 **/
public final class FailoverNode {
    private static final String FAILOVER = "failover";
    private static final String LEADER_ROOT = LeaderNode.ROOT + "/" + FAILOVER;
    static final String ITEMS_ROOT = LEADER_ROOT + "/items";
    private static final String ITEMS = ITEMS_ROOT +"/%s";
    static final String LATCH = LEADER_ROOT + "/latch";
    private static final String EXECUTION_FAILOVER = ShardingNode.ROOT + "/%s/" + FAILOVER;
    private final JobNodePath jobNodePath;

    public FailoverNode(String jobName){
        jobNodePath = new JobNodePath(jobName);
    }

    static String getItemsNode(int item) {
        return String.format(ITEMS, item);
    }

    static String getExecutionFailoverNode(int item) {
        return String.format(EXECUTION_FAILOVER, item);
    }

    /** 功能描述: 通过执行故障转移路径获取分片项
    *
    * @param: [path：故障转移路径]
    * @return: 分片项，如果不是从故障转移路径返回null
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/19 14:45
    */
    public Integer getItemByExecutionFailoverPath(String path){
        if (!isFailoverPath(path)){
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ShardingNode.ROOT).length()+1, path.lastIndexOf(FailoverNode.FAILOVER)-1));
    }

    private boolean isFailoverPath(String path){
        return path.startsWith(jobNodePath.getFullPath(ShardingNode.ROOT)) && path.endsWith(FailoverNode.FAILOVER);
    }
}
