package com.simba.elasticjob.tracing.listener;

import com.simba.elasticjob.tracing.api.TracingConfiguration;
import com.simba.elasticjob.tracing.exception.TracingConfigurationException;
import com.simba.elasticjob.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 19:35
 * @Version V1.0
 **/
public final class TracingListenerFactory {
    private TracingListenerFactory() {}

    private static final Map<String, TracingListenerConfiguration> LISTENER_CONFIGS = new HashMap<>();

    static {
        for (TracingListenerConfiguration each : ServiceLoader.load(TracingListenerConfiguration.class)) {
            LISTENER_CONFIGS.put(each.getType(), each);
        }
    }

    /**
     * Get tracing listener.
     *
     * @param tracingConfig tracing configuration
     * @return tracing listener
     * @throws TracingConfigurationException tracing configuration exception
     */
    public static TracingListener getListener(final TracingConfiguration tracingConfig) throws TracingConfigurationException {
        if (null == tracingConfig.getTracingStorageConfiguration() || StringUtils.isNullOrEmpty(tracingConfig.getType()) || !LISTENER_CONFIGS.containsKey(tracingConfig.getType())) {

            throw new TracingConfigurationException(String.format("Can not find executor service handler type '%s'.", tracingConfig.getType()));
        }
        return LISTENER_CONFIGS.get(tracingConfig.getType()).createTracingListener(tracingConfig.getTracingStorageConfiguration().getStorage());
    }
}
