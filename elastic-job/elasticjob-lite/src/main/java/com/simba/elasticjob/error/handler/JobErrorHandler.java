package com.simba.elasticjob.error.handler;

import com.simba.elasticjob.spi.SPIPostProcessor;
import com.simba.elasticjob.spi.TypedSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Job error handler.
 */
public interface JobErrorHandler extends TypedSPI, SPIPostProcessor, Closeable {
    Logger log = LoggerFactory.getLogger(JobErrorHandler.class);
    /**
     * Handle exception.
     * 
     * @param jobName job name
     * @param cause failure cause
     */
    void handleException(String jobName, Throwable cause);
    
    @Override
    default void close() {
    }
}
