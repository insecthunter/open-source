package com.simba.elasticjob.tracing.listener;

import com.simba.elasticjob.tracing.exception.TracingConfigurationException;

/**
 * @Description Tracing listener configuration.
 *
 * @param <T> type of tracing storage
 * @Author yuanjx3
 * @Date 2021/1/21 19:36
 * @Version V1.0
 **/
public interface TracingListenerConfiguration<T> {
    /**
     * Create tracing listener.
     *
     * @param storage storage
     * @return tracing listener
     * @throws TracingConfigurationException tracing configuration exception
     */
    TracingListener createTracingListener(T storage) throws TracingConfigurationException;

    /**
     * Get tracing type.
     *
     * @return tracing type
     */
    String getType();
}
