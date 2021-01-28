package com.simba.elasticjob.configuration.pojo;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.configuration.JobExtraConfiguration;
import com.simba.elasticjob.internal.yaml.YamlConfiguration;
import com.simba.elasticjob.internal.yaml.YamlConfigurationConverterFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * @Description job配置信息存储实体类
 * @Author yuanjx3
 * @Date 2021/1/15 10:58
 * @Version V1.0
 **/
public class JobConfigurationPOJO {
    private String jobName;
    private String cron;
    private int shardingTotalCount;
    private String shardingItemParameters;
    private String jobParameter;
    private boolean monitorExecution;
    private boolean failover;
    private boolean misfire;
    private int maxTimeDiffSeconds = -1;
    private int reconcileIntervalMinutes;
    private String jobShardingStrategyType;
    private String jobExecutorServiceHandlerType;
    private String jobErrorHandlerType;
    private Collection<String> jobListenerTypes = new ArrayList<>();
    private Collection<YamlConfiguration<JobExtraConfiguration>> jobExtraConfigurations = new LinkedList<>();
    private String description;
    private Properties prop = new Properties();
    private boolean disabled;
    private boolean overwrite;

    /** 功能描述:  JobConfigurationPOJO 转为 JobConfiguration 对象
    * @Author: yuanjx3
    * @Date: 2021/1/15 11:09
    */
    public JobConfiguration toJobConfiguration(){
        JobConfiguration jobConfig = JobConfiguration.newBuilder(jobName, shardingTotalCount)
                .cron(cron)
                .jobParameter(jobParameter)
                .monitorExecution(monitorExecution)
                .failover(failover)
                .misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .reconcileIntervalMinutes(reconcileIntervalMinutes)
                .jobShardingStrategyType(jobShardingStrategyType)
                .jobExecutorServiceHandlerType(jobExecutorServiceHandlerType)
                .jobErrorHandlerType(jobErrorHandlerType)
                .jobListenerTypes(jobListenerTypes.toArray(new String[]{}))
                .description(description)
                .disabled(disabled)
                .overwrite(overwrite)
                .build();

        prop.keySet().stream().forEach((key)->jobConfig.getProps().setProperty(key.toString(), prop.get(key.toString()).toString()));
        jobExtraConfigurations.stream()
                .map((config)->config.toConfiguration())
                .forEach((config)->jobConfig.getExtraConfigurations().add(config));

        return jobConfig;
    }

    /** 功能描述:  JobConfiguration 转为 JobConfigurationPOJO 对象
     * @Author: yuanjx3
     * @Date: 2021/1/15 11:09
     */
    public static JobConfigurationPOJO fromJobConfiguration(JobConfiguration jobConfig){
        JobConfigurationPOJO jobConfigurationPOJO = new JobConfigurationPOJO();
        jobConfigurationPOJO.setJobName(jobConfig.getJobName());
        jobConfigurationPOJO.setShardingTotalCount(jobConfig.getShardingTotalCount());
        jobConfigurationPOJO.setCron(jobConfig.getCron());
        jobConfigurationPOJO.setJobParameter(jobConfig.getJobParameter());
        jobConfigurationPOJO.setMonitorExecution(jobConfig.isMonitorExecution());
        jobConfigurationPOJO.setFailover(jobConfig.isFailover());
        jobConfigurationPOJO.setMisfire(jobConfig.isMisfire());
        jobConfigurationPOJO.setMaxTimeDiffSeconds(jobConfig.getMaxTimeDiffSeconds());
        jobConfigurationPOJO.setReconcileIntervalMinutes(jobConfig.getReconcileIntervalMinutes());
        jobConfigurationPOJO.setJobShardingStrategyType(jobConfig.getJobShardingStrategyType());
        jobConfigurationPOJO.setJobExecutorServiceHandlerType(jobConfig.getJobExecutorServiceHandlerType());
        jobConfigurationPOJO.setJobErrorHandlerType(jobConfig.getJobErrorHandlerType());
        jobConfigurationPOJO.setJobListenerTypes(jobConfig.getJobListenerTypes());
        jobConfigurationPOJO.setDescription(jobConfig.getDescription());
        jobConfigurationPOJO.setDisabled(jobConfig.isDisabled());
        jobConfigurationPOJO.setOverwrite(jobConfig.isOverwrite());
        jobConfigurationPOJO.setProp(jobConfig.getProps());
        /**
         * 这里又将集合中的JobExtraConfiguration对象循环转为Yaml格式字符串然后放入集合，再赋值给POJO的jobExtraConfigurations属性
         * 这里我就直接将源码中转yaml的几个类copy过来
         */
        jobConfig.getExtraConfigurations().stream()
                .map(each->
                        YamlConfigurationConverterFactory
                                .findConverter((Class<JobExtraConfiguration>)each.getClass())
                                .orElseThrow(()->new RuntimeException("没有找到合适的YAML转换工具类！"))
                                .convertToYamlConfiguration(each))
                .forEach(jobConfigurationPOJO.getJobExtraConfigurations()::add);
        return jobConfigurationPOJO;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    public void setShardingTotalCount(int shardingTotalCount) {
        this.shardingTotalCount = shardingTotalCount;
    }

    public String getShardingItemParameters() {
        return shardingItemParameters;
    }

    public void setShardingItemParameters(String shardingItemParameters) {
        this.shardingItemParameters = shardingItemParameters;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public void setJobParameter(String jobParameter) {
        this.jobParameter = jobParameter;
    }

    public boolean isMonitorExecution() {
        return monitorExecution;
    }

    public void setMonitorExecution(boolean monitorExecution) {
        this.monitorExecution = monitorExecution;
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public boolean isMisfire() {
        return misfire;
    }

    public void setMisfire(boolean misfire) {
        this.misfire = misfire;
    }

    public int getMaxTimeDiffSeconds() {
        return maxTimeDiffSeconds;
    }

    public void setMaxTimeDiffSeconds(int maxTimeDiffSeconds) {
        this.maxTimeDiffSeconds = maxTimeDiffSeconds;
    }

    public int getReconcileIntervalMinutes() {
        return reconcileIntervalMinutes;
    }

    public void setReconcileIntervalMinutes(int reconcileIntervalMinutes) {
        this.reconcileIntervalMinutes = reconcileIntervalMinutes;
    }

    public String getJobShardingStrategyType() {
        return jobShardingStrategyType;
    }

    public void setJobShardingStrategyType(String jobShardingStrategyType) {
        this.jobShardingStrategyType = jobShardingStrategyType;
    }

    public String getJobExecutorServiceHandlerType() {
        return jobExecutorServiceHandlerType;
    }

    public void setJobExecutorServiceHandlerType(String jobExecutorServiceHandlerType) {
        this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
    }

    public String getJobErrorHandlerType() {
        return jobErrorHandlerType;
    }

    public void setJobErrorHandlerType(String jobErrorHandlerType) {
        this.jobErrorHandlerType = jobErrorHandlerType;
    }

    public Collection<String> getJobListenerTypes() {
        return jobListenerTypes;
    }

    public void setJobListenerTypes(Collection<String> jobListenerTypes) {
        this.jobListenerTypes = jobListenerTypes;
    }

    public Collection<YamlConfiguration<JobExtraConfiguration>> getJobExtraConfigurations() {
        return jobExtraConfigurations;
    }

    public void setJobExtraConfigurations(Collection<YamlConfiguration<JobExtraConfiguration>> jobExtraConfigurations) {
        this.jobExtraConfigurations = jobExtraConfigurations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Properties getProp() {
        return prop;
    }

    public void setProp(Properties prop) {
        this.prop = prop;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
