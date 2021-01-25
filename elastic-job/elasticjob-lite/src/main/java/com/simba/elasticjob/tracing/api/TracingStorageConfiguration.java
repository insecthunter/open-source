package com.simba.elasticjob.tracing.api;

/**
 * @Description Tracing storage configuration.
 *
 * @param <T> storage type
 * @Author yuanjx3
 * @Date 2021/1/21 19:24
 * @Version V1.0
 **/
public interface TracingStorageConfiguration<T> {
    /**
     * Create storage.
     *
     * @return storage
     */
    T getStorage();
}
