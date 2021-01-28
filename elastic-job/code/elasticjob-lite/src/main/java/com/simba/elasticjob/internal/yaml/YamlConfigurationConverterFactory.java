package com.simba.elasticjob.internal.yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 14:24
 * @Version V1.0
 **/
public class YamlConfigurationConverterFactory {
    private static final Map<Class<?>, YamlConfigurationConverter<?, ?>> CONVERTERS = new LinkedHashMap<>();

    static {
        ServiceLoader.load(YamlConfigurationConverter.class).forEach(each -> CONVERTERS.put(each.configurationType(), each));
    }

    /**
     * Find {@link YamlConfigurationConverter} for specific configuration type.
     *
     * @param configurationType type of configuration
     * @param <T> type of configuration
     * @param <Y> type of YAML configuration
     * @return converter for specific configuration type
     */
    @SuppressWarnings("unchecked")
    public static <T, Y extends YamlConfiguration<T>> Optional<YamlConfigurationConverter<T, Y>> findConverter(final Class<T> configurationType) {
        return Optional.ofNullable((YamlConfigurationConverter<T, Y>) CONVERTERS.get(configurationType));
    }
}
