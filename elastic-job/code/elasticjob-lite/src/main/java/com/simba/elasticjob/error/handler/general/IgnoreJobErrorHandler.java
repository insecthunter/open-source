package com.simba.elasticjob.error.handler.general;

import com.simba.elasticjob.error.handler.JobErrorHandler;

import java.util.Properties;

/**
 * Job error handler for ignore exception.
 */
public final class IgnoreJobErrorHandler implements JobErrorHandler {
    
    @Override
    public void init(final Properties props) {
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
    }
    
    @Override
    public String getType() {
        return "IGNORE";
    }
}
