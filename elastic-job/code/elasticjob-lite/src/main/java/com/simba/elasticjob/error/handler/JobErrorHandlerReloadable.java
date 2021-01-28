package com.simba.elasticjob.error.handler;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.executor.Reloadable;
import com.simba.elasticjob.executor.ReloadablePostProcessor;
import com.simba.elasticjob.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

/**
 * @Description JobErrorHandler Reloadable
 * @Author yuanjx3
 * @Date 2021/1/22 8:46
 * @Version V1.0
 **/
public class JobErrorHandlerReloadable implements Reloadable<JobErrorHandler>, ReloadablePostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String jobErrorHandlerType;

    private Properties props;

    private JobErrorHandler jobErrorHandler;

    @Override
    public void init(final JobConfiguration jobConfig) {
        jobErrorHandlerType = StringUtils.isNullOrEmpty(jobConfig.getJobErrorHandlerType()) ? JobErrorHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobErrorHandlerType();
        props = (Properties) jobConfig.getProps().clone();
        jobErrorHandler = JobErrorHandlerFactory.createHandler(jobErrorHandlerType, props)
                .orElseThrow(() -> new JobConfigurationException("Cannot find job error handler type '%s'.", jobErrorHandlerType));
    }

    @Override
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        String newJobErrorHandlerType = StringUtils.isNullOrEmpty(jobConfig.getJobErrorHandlerType()) ? JobErrorHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobErrorHandlerType();
        if (newJobErrorHandlerType.equals(jobErrorHandlerType) && props.equals(jobConfig.getProps())) {
            return;
        }
        log.debug("JobErrorHandler reload occurred in the job '{}'. Change from '{}' to '{}'.", jobConfig.getJobName(), jobErrorHandlerType, newJobErrorHandlerType);
        reload(newJobErrorHandlerType, jobConfig.getProps());
    }

    private void reload(final String jobErrorHandlerType, final Properties props) {
        jobErrorHandler.close();
        this.jobErrorHandlerType = jobErrorHandlerType;
        this.props = (Properties) props.clone();
        jobErrorHandler = JobErrorHandlerFactory.createHandler(jobErrorHandlerType, props)
                .orElseThrow(() -> new JobConfigurationException("Cannot find job error handler type '%s'.", jobErrorHandlerType));
    }

    @Override
    public JobErrorHandler getInstance() {
        return jobErrorHandler;
    }

    @Override
    public String getType() {
        return JobErrorHandler.class.getName();
    }

    @Override
    public void close() {
        Optional.ofNullable(jobErrorHandler).ifPresent(JobErrorHandler::close);
    }
}
