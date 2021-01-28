package com.simba.elasticjob.error.handler.general;

import com.simba.elasticjob.error.handler.JobErrorHandler;

import java.util.Properties;

/**
 * Job error handler for log error message.
 */
public final class LogJobErrorHandler implements JobErrorHandler {
    
    @Override
    public void init(final Properties props) {
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        log.error(String.format("Job '%s' exception occur in job processing", jobName), cause);
    }
    
    @Override
    public String getType() {
        return "LOG";
    }
}
