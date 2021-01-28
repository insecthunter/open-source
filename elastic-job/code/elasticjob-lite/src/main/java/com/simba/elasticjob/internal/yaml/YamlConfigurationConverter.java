package com.simba.elasticjob.internal.yaml;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 14:25
 * @Version V1.0
 **/
public interface YamlConfigurationConverter<T, Y extends YamlConfiguration<T>> {

    /**
     * Convert to YAML configuration.
     *
     * @param data data to be converted
     * @return YAML configuration
     */
    Y convertToYamlConfiguration(T data);

    /**
     * Get type of Configuration.
     *
     * @return configuration type
     */
    Class<T> configurationType();
}
