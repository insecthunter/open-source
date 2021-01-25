package com.simba.elasticjob.internal.guarantee;

import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description Guarantee node
 * @Author yuanjx3
 * @Date 2021/1/19 17:07
 * @Version V1.0
 **/
public final class GuaranteeNode {
    private static final String ROOT = "guarantee";
    static final String STARTED_ROOT = ROOT + "/started";
    static final String COMPLETED_ROOT = ROOT + "/completed";
    private final JobNodePath jobNodePath;

    GuaranteeNode(String jobName){
        jobNodePath = new JobNodePath(jobName);
    }

    static String getStartedNode(int shardingItem){
        return String.join("/", STARTED_ROOT, shardingItem + "");
    }

    static String getCompletedNode(int shardingItem){
        return String.join("/", COMPLETED_ROOT, shardingItem + "");
    }

    boolean isStartedRootNode(String path) {
        return jobNodePath.getFullPath(STARTED_ROOT).equals(path);
    }

    boolean isCompletedRootNode(String path) {
        return jobNodePath.getFullPath(COMPLETED_ROOT).equals(path);
    }
}
