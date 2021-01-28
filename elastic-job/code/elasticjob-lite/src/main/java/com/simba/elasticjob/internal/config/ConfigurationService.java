package com.simba.elasticjob.internal.config;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.configuration.pojo.JobConfigurationPOJO;
import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.exception.JobExecutionEnvironmentException;
import com.simba.elasticjob.internal.yaml.YamlEngine;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.internal.storage.JobNodeStorage;

/**
 * @Description job配置操作服务类
 * @Author yuanjx3
 * @Date 2021/1/14 20:06
 * @Version V1.0
 **/
public class ConfigurationService {
    private final TimeService timeService;
    private final JobNodeStorage jobNodeStorage;

    public ConfigurationService(CoordinatorRegistryCenter registryCenter, String jobName) {
        this.jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
        this.timeService = new TimeService();
    }

    /** 加载job配置 */
    public JobConfiguration load(boolean fromCache){
        String result = null;
        if (fromCache){
            //获取配置信息根节点数据
            result = jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT);
        }
        if (null == result){
            result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
        }
        return YamlEngine.unmarshal(result, JobConfigurationPOJO.class).toJobConfiguration();
    }

    /** 设置job配置，并重载新的配置信息 **/
    public JobConfiguration setUpJobConfiguration(String jobClassName,JobConfiguration jobConfig){
        checkConflictJob(jobClassName, jobConfig);
        if (!jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT) || jobConfig.isOverwrite()){
            jobNodeStorage.replaceJobNode(ConfigurationNode.ROOT, YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
            jobNodeStorage.replaceJobRootNode(jobClassName);
            return jobConfig;
        }
        return load(false);
    }
    
    /** 功能描述:  检查job是否冲突
    * @Author: yuanjx3
    * @Date: 2021/1/15 14:59
    */
    private void checkConflictJob(String newJobClassName, JobConfiguration jobConfig){
        //job根节点不存在，则不存在冲突
        if(!jobNodeStorage.isJobRootNodeExisted()){
            return;
        }
        String oldJobClassName = jobNodeStorage.getJobRootNodeData();
        //判断传入的 jobClassName 与 当前注册中心中的 jobClassName是否相同，不同则冲突，抛出异常
        if (null!=oldJobClassName && !oldJobClassName.equals(newJobClassName)){
            throw new JobConfigurationException("job['%s']冲突： 注册中心中的job-class-name 是 ['%s'], 传入的job-class-name 是 ['%s']"
                    , jobConfig.getJobName(),oldJobClassName, newJobClassName);
        }
    }

    /** 检查作业服务器与注册中心之间最大允许时差 **/
    public void checkMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        int maxTimeDiffSeconds = load(true).getMaxTimeDiffSeconds();
        if (0>maxTimeDiffSeconds){
            return;
        }
        long timeDiff = Math.abs(timeService.getCurrentMillis() - jobNodeStorage.getRegistryCenterTime());
        if (timeDiff>maxTimeDiffSeconds*1000L){
            throw new JobExecutionEnvironmentException("job server 和注册中心上的时间差是 '%s' seconds, 最大时间差是 '%s' seconds", timeDiff/1000,maxTimeDiffSeconds);
        }
    }
}
