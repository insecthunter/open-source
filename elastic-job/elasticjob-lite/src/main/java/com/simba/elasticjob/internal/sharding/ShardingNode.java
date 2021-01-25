package com.simba.elasticjob.internal.sharding;

import com.simba.elasticjob.internal.election.LeaderNode;
import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description 分片数据节点
 * @Author yuanjx3
 * @Date 2021/1/17 16:52
 * @Version V1.0
 **/
public final class ShardingNode {
    public static final String ROOT = "sharding";
    private static final String INSTANCE_APPENDIX = "instance";
    private static final String INSTANCE = ROOT + "/%s/" + INSTANCE_APPENDIX;
    private static final String RUNNING_APPENDIX = "running";
    private static final String RUNNING = ROOT + "/%s/" + RUNNING_APPENDIX;
    private static final String MISFIRE = ROOT + "/%s/misfire";
    private static final String DISABLED = ROOT + "/%s/disabled";
    private static final String LEADER_ROOT = LeaderNode.ROOT + "/" + ROOT;
    static final String NECESSARY = LEADER_ROOT + "/necessary";
    static final String PROCESSING = LEADER_ROOT + "/processing";
    private final JobNodePath jobNodePath;

    public ShardingNode(String jobName){
        jobNodePath = new JobNodePath(jobName);
    }

    /** 功能描述:   获取分片项的作业实例运行节点的path
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:01
    */
    public static String getInstanceNodePath(int item){
        return String.format(INSTANCE, item);
    }

    /** 功能描述: 获得分片作业运行节点的path
     * @Author: yuanjx3
     * @Date: 2021/1/17 17:01
     */
    public static String getRunningNode(int item){
        return String.format(RUNNING, item);
    }

    static String getMisfireNode(int item){
        return String.format(MISFIRE, item);
    }

    static String getDisabledNode(int item){
        return String.format(DISABLED, item);
    }
    
    /** 功能描述: 
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:07
    */
    public Integer getDisabledNode(String path){
        if (!isRunningItemPath(path)){
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ROOT).length() + 1, path.lastIndexOf(RUNNING_APPENDIX) - 1));
    }
    
    /** 功能描述:  判断是否是运行节点的path
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/17 17:12
    */
    private boolean isRunningItemPath(String path) {
        return path.startsWith(jobNodePath.getFullPath(ROOT)) && path.endsWith(RUNNING_APPENDIX);
    }
}
