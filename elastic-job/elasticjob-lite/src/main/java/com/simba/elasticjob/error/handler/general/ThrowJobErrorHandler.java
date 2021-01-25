package com.simba.elasticjob.error.handler.general;

import com.simba.elasticjob.error.handler.JobErrorHandler;
import com.simba.elasticjob.exception.JobSystemException;

import java.util.Properties;

/**
 * Job error handler for throw exception.
 */
public final class ThrowJobErrorHandler implements JobErrorHandler {
    
    @Override
    public void init(final Properties props) {
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        throw new JobSystemException(cause);
    }
    
    @Override
    public String getType() {
        return "THROW";
    }
}
