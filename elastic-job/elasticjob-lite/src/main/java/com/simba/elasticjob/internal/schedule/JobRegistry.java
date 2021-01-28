package com.simba.elasticjob.internal.schedule;


import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description job 注册中心
 * @Author yuanjx3
 * @Date 2021/1/15 16:08
 * @Version V1.0
 **/
public class JobRegistry {
    private static volatile JobRegistry instance;
    private Map<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();
    private Map<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();
    private Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();
    private Map<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();
    private Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();

    public static JobRegistry getInstance(){
        if (null==instance){
            synchronized (JobRegistry.class){
                if (null==instance){
                    instance = new JobRegistry();
                }
            }
        }
        return instance;
    }

    /** 功能描述: 注册新注册中心
    * @Author: yuanjx3
    * @Date: 2021/1/15 17:13
    */
    public void registerRegistryCenter(String jobName,CoordinatorRegistryCenter registryCenter){
        regCenterMap.put(jobName,registryCenter);
        registryCenter.addCacheData("/"+jobName);
    }

    /** 功能描述: 注册新作业
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public void registerJob(String jobName,JobScheduleController jobScheduleController){
        schedulerMap.put(jobName,jobScheduleController);
    }

    /** 功能描述: 根据任务名称获取任务调度控制器
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public JobScheduleController getJobScheduleController(String jobName){
        return schedulerMap.get(jobName);
    }

    /** 功能描述: 根据任务名称获取注册中心
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public CoordinatorRegistryCenter getRegCenter(String jobName){
        return regCenterMap.get(jobName);
    }

    /** 功能描述: 添加一个任务实例
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public void addJobInstance(String jobName, JobInstance jobInstance){
        jobInstanceMap.put(jobName, jobInstance);
    }

    /** 功能描述: 通过jobName 获取一个任务实例
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public JobInstance getJobInstance(String jobName){
        return jobInstanceMap.get(jobName);
    }

    /** 功能描述: 判断jobName对应的job是否正则执行
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public boolean isJobRunning(String jobName){
        return jobRunningMap.getOrDefault(jobName, false);
    }

    /** 功能描述: 添加一个运行状态的job
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public void setJobRunning(String jobName, boolean isRunning){
        jobRunningMap.put(jobName, isRunning);
    }

    /** 功能描述: 获取运行在当前任务服务器上的总分片数
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public int getCurrentShardingTotalCount(String jobName){
        return currentShardingTotalCountMap.getOrDefault(jobName, 0);
    }

    /** 功能描述: 设置运行在当前任务服务器上的总分片数
     * @Author: yuanjx3
     * @Date: 2021/1/15 17:13
     */
    public void setCurrentShardingTotalCount(String jobName, int currentShardingTotalCount){
        currentShardingTotalCountMap.put(jobName, currentShardingTotalCount);
    }
    
    /** 功能描述: 关闭作业调度
    * @Author: yuanjx3
    * @Date: 2021/1/15 17:27
    */
    public void shutdown(String jobName){
        //通过任务调度控制器关闭任务调度器
        Optional.ofNullable(schedulerMap.remove(jobName)).ifPresent(JobScheduleController::shutdown);
        //删除作业的注册中心，并且取消jobName的节点监听
        Optional.ofNullable(regCenterMap.remove(jobName)).ifPresent(regCenter->regCenter.evictCacheData("/"+jobName));
        //删除集合中的作业实例数据
        jobInstanceMap.remove(jobName);
        //删除集合中的正在运行实例数据
        jobRunningMap.remove(jobName);
        //清理任务的分片数据
        currentShardingTotalCountMap.remove(jobName);
    }
    
    /** 功能描述:   判断一个任务实例是否关闭
    * @Author: yuanjx3
    * @Date: 2021/1/15 17:34
    */
    public boolean isShutdown(String jobName){
        return !schedulerMap.containsKey(jobName) || !jobInstanceMap.containsKey(jobName);
    }
}










