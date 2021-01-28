package com.simba.elasticjob.executor;


import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.spi.TypedSPI;

import java.io.Closeable;

/**
 * @Description Reloadable.
 *
 * @param <T> reload target
 * @Author yuanjx3
 * @Date 2021/1/22 8:40
 * @Version V1.0
 **/
public interface Reloadable<T> extends TypedSPI, Closeable {

    /**
     * Reload if necessary.
     *
     * @param jobConfiguration job configuration
     */
    void reloadIfNecessary(JobConfiguration jobConfiguration);

    /**
     * Get target instance.
     *
     * @return instance
     */
    T getInstance();
}
