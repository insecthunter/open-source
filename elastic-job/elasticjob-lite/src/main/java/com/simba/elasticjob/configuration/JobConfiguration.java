package com.simba.elasticjob.configuration;

import com.simba.elasticjob.utils.Preconditions;
import com.simba.elasticjob.utils.StringUtils;

import java.util.*;

/**
 * @Description 定时任务配置类
 * @Author yuanjx3
 * @Date 2021/1/13 19:46
 * @Version V1.0
 **/
public final class JobConfiguration {
    private final String jobName;
    private final String cron;
    private final int shardingTotalCount;
    private final String shardingItemParameters;
    private final String jobParameter;
    private final boolean monitorExecution;
    private final boolean failover;
    private final boolean misfire;
    private final int maxTimeDiffSeconds;
    private final int reconcileIntervalMinutes;
    private final String jobShardingStrategyType;
    private final String jobExecutorServiceHandlerType;
    private final String jobErrorHandlerType;
    private final Collection<String> jobListenerTypes;
    private final Collection<JobExtraConfiguration> extraConfigurations;
    private final String description;
    private final Properties props;
    private final boolean disabled;
    private final boolean overwrite;

    private JobConfiguration(String jobName, String cron, int shardingTotalCount, String shardingItemParameters, String jobParameter, boolean monitorExecution, boolean failover, boolean misfire, int maxTimeDiffSeconds, int reconcileIntervalMinutes, String jobShardingStrategyType, String jobExecutorServiceHandlerType, String jobErrorHandlerType, Collection<String> jobListenerTypes, Collection<JobExtraConfiguration> extraConfigurations, String description, Properties props, boolean disabled, boolean overwrite) {
        this.jobName = jobName;
        this.cron = cron;
        this.shardingTotalCount = shardingTotalCount;
        this.shardingItemParameters = shardingItemParameters;
        this.jobParameter = jobParameter;
        this.monitorExecution = monitorExecution;
        this.failover = failover;
        this.misfire = misfire;
        this.maxTimeDiffSeconds = maxTimeDiffSeconds;
        this.reconcileIntervalMinutes = reconcileIntervalMinutes;
        this.jobShardingStrategyType = jobShardingStrategyType;
        this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
        this.jobErrorHandlerType = jobErrorHandlerType;
        this.jobListenerTypes = jobListenerTypes;
        this.extraConfigurations = extraConfigurations;
        this.description = description;
        this.props = props;
        this.disabled = disabled;
        this.overwrite = overwrite;
    }

    public static Builder newBuilder(String jobName, int shardingTotalCount){
        return new Builder(jobName,shardingTotalCount);
    }

    public String getJobName() {
        return jobName;
    }

    public String getCron() {
        return cron;
    }

    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    public String getShardingItemParameters() {
        return shardingItemParameters;
    }

    public String getJobParameter() {
        return jobParameter;
    }

    public boolean isMonitorExecution() {
        return monitorExecution;
    }

    public boolean isFailover() {
        return failover;
    }

    public boolean isMisfire() {
        return misfire;
    }

    public int getMaxTimeDiffSeconds() {
        return maxTimeDiffSeconds;
    }

    public int getReconcileIntervalMinutes() {
        return reconcileIntervalMinutes;
    }

    public String getJobShardingStrategyType() {
        return jobShardingStrategyType;
    }

    public String getJobExecutorServiceHandlerType() {
        return jobExecutorServiceHandlerType;
    }

    public String getJobErrorHandlerType() {
        return jobErrorHandlerType;
    }

    public Collection<String> getJobListenerTypes() {
        return jobListenerTypes;
    }

    public Collection<JobExtraConfiguration> getExtraConfigurations() {
        return extraConfigurations;
    }

    public String getDescription() {
        return description;
    }

    public Properties getProps() {
        return props;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public static class Builder {
        private final String jobName;
        private String cron;
        private final int shardingTotalCount;
        private String shardingItemParameters;
        private String jobParameter;
        private boolean monitorExecution;
        private boolean failover;
        private boolean misfire;
        private int maxTimeDiffSeconds;
        private int reconcileIntervalMinutes;
        private String jobShardingStrategyType;
        private String jobExecutorServiceHandlerType;
        private String jobErrorHandlerType;
        private final Collection<String> jobListenerTypes;
        private final Collection<JobExtraConfiguration> extraConfigurations;
        private String description;
        private final Properties props;
        private boolean disabled;
        private boolean overwrite;

        public JobConfiguration.Builder cron(String cron) {
            if (null != cron) {
                this.cron = cron;
            }
            return this;
        }
        public JobConfiguration.Builder shardingItemParameters(String shardingItemParameters) {
            if (null != shardingItemParameters) {
                this.shardingItemParameters = shardingItemParameters;
            }
            return this;
        }
        public JobConfiguration.Builder jobParameter(String jobParameter) {
            if (null != jobParameter) {
                this.jobParameter = jobParameter;
            }
            return this;
        }
        public JobConfiguration.Builder monitorExecution(boolean monitorExecution) {
            this.monitorExecution = monitorExecution;
            return this;
        }
        public JobConfiguration.Builder failover(boolean failover) {
            this.failover = failover;
            return this;
        }
        public JobConfiguration.Builder misfire(boolean misfire) {
            this.misfire = misfire;
            return this;
        }
        public JobConfiguration.Builder maxTimeDiffSeconds(int maxTimeDiffSeconds) {
            this.maxTimeDiffSeconds = maxTimeDiffSeconds;
            return this;
        }
        public JobConfiguration.Builder reconcileIntervalMinutes(int reconcileIntervalMinutes) {
            this.reconcileIntervalMinutes = reconcileIntervalMinutes;
            return this;
        }
        public JobConfiguration.Builder jobShardingStrategyType(String jobShardingStrategyType) {
            if (null != jobShardingStrategyType) {
                this.jobShardingStrategyType = jobShardingStrategyType;
            }
            return this;
        }
        public JobConfiguration.Builder jobExecutorServiceHandlerType(String jobExecutorServiceHandlerType) {
            this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
            return this;
        }
        public JobConfiguration.Builder jobErrorHandlerType(String jobErrorHandlerType) {
            this.jobErrorHandlerType = jobErrorHandlerType;
            return this;
        }
        public JobConfiguration.Builder jobListenerTypes(String... jobListenerTypes) {
            this.jobListenerTypes.addAll(Arrays.asList(jobListenerTypes));
            return this;
        }
        public JobConfiguration.Builder addExtraConfig(JobExtraConfiguration extraConfig) {
            this.extraConfigurations.add(extraConfig);
            return this;
        }
        public JobConfiguration.Builder description(String description) {
            if (null != description) {
                this.description = description;
            }
            return this;
        }
        public JobConfiguration.Builder setProperty(String key, String  value) {
            this.props.setProperty(key,value);
            return this;
        }
        public JobConfiguration.Builder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }
        public JobConfiguration.Builder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public final JobConfiguration build(){
            Preconditions.checkArgument(StringUtils.isNotNullOrEmpty(this.jobName),"jobName 不能为空！");
            Preconditions.checkArgument(this.shardingTotalCount>0,"jobName 不能为空！");
            return new JobConfiguration(this.jobName, this.cron, this.shardingTotalCount, this.shardingItemParameters, this.jobParameter, this.monitorExecution, this.failover, this.misfire, this.maxTimeDiffSeconds, this.reconcileIntervalMinutes, this.jobShardingStrategyType, this.jobExecutorServiceHandlerType, this.jobErrorHandlerType, this.jobListenerTypes, this.extraConfigurations, this.description, this.props, this.disabled, this.overwrite);
        }
        private Builder(String jobName, int shardingTotalCount) {
            this.shardingItemParameters = "";
            this.jobParameter = "";
            this.monitorExecution = true;
            this.misfire = true;
            this.maxTimeDiffSeconds = -1;
            this.reconcileIntervalMinutes = 10;
            this.jobListenerTypes = new ArrayList();
            this.extraConfigurations = new LinkedList();
            this.description = "";
            this.props = new Properties();
            this.jobName = jobName;
            this.shardingTotalCount = shardingTotalCount;
        }
    }
}
