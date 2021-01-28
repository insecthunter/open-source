package com.simba.elasticjob.tracing.api;

import com.simba.elasticjob.configuration.JobExtraConfiguration;
import com.simba.elasticjob.tracing.exception.TracingStorageConverterNotFoundException;
import com.simba.elasticjob.tracing.storage.TracingStorageConverterFactory;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 19:22
 * @Version V1.0
 **/
public class TracingConfiguration<T> implements JobExtraConfiguration {
    private final String type;

    private final TracingStorageConfiguration<T> tracingStorageConfiguration;

    public TracingConfiguration(final String type, final T storage) {
        this.type = type;
        this.tracingStorageConfiguration = TracingStorageConverterFactory.findConverter((Class<T>) storage.getClass())
                .orElseThrow(() -> new TracingStorageConverterNotFoundException(storage.getClass())).convertObjectToConfiguration(storage);
    }

    public TracingConfiguration(String type, TracingStorageConfiguration<T> tracingStorageConfiguration) {
        this.type = type;
        this.tracingStorageConfiguration = tracingStorageConfiguration;
    }

    public String getType() {
        return type;
    }

    public TracingStorageConfiguration<T> getTracingStorageConfiguration() {
        return tracingStorageConfiguration;
    }
}
