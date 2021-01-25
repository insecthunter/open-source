package com.simba.elasticjob.tracing.storage;

import com.simba.elasticjob.tracing.api.TracingStorageConfiguration;

/**
 * @Description Tracing storage converter.
 *
 * @param <T> storage type
 * @Author yuanjx3
 * @Date 2021/1/21 19:28
 * @Version V1.0
 **/
public interface TracingStorageConverter<T> {

    /**
     * Convert storage to {@link TracingStorageConfiguration}.
     *
     * @param storage storage instance
     * @return instance of {@link TracingStorageConfiguration}
     */
    TracingStorageConfiguration<T> convertObjectToConfiguration(T storage);

    /**
     * Storage type.
     *
     * @return class of storage
     */
    Class<T> storageType();
}
