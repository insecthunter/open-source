package com.simba.elasticjob.internal.instance;

import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description 运行实例节点类
 * @Author yuanjx3
 * @Date 2021/1/15 20:17
 * @Version V1.0
 **/
public class InstanceNode {
    public static final String ROOT = "instances";
    private static final String INSTANCES = ROOT + "/%s";
    private final String jobName;
    private final JobNodePath jobNodePath;

    public InstanceNode(String jobName) {
        this.jobName = jobName;
        this.jobNodePath = new JobNodePath(jobName);
    }

    /** 功能描述:   获取任务工作服务器实例的root节点全路径
    * @Author: yuanjx3
    * @Date: 2021/1/15 20:21
    */
    public String getInstancesFullPath(){
        return  jobNodePath.getFullPath(InstanceNode.ROOT);
    }

    /** 功能描述:   判断传入的path是否为job工作服务器实例的root节点路径
     * @Author: yuanjx3
     * @Date: 2021/1/15 20:21
     */
    public boolean isInstancesPath(String path){
        return  path.startsWith(jobNodePath.getFullPath(InstanceNode.ROOT));
    }

    /** 功能描述:   判断传入的path是否为本地job工作服务器实例的root节点路径
     * @Author: yuanjx3
     * @Date: 2021/1/15 20:21
     */
    public boolean isLocalInstancesPath(String path){
        return  path.equals(jobNodePath.getFullPath(String.format(INSTANCES, JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId())));
    }

    String getLocalInstancePath() {
        return getInstancePath(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId());
    }

    String getInstancePath(final String instanceId) {
        return String.format(INSTANCES, instanceId);
    }
}
