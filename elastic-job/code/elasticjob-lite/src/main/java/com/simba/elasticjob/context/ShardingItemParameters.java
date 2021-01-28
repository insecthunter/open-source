package com.simba.elasticjob.context;

import com.simba.elasticjob.exception.JobConfigurationException;
import com.simba.elasticjob.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description  分片项参数
 * @Author yuanjx3
 * @Date 2021/1/21 16:24
 * @Version V1.0
 **/
public final class ShardingItemParameters {
    private static final String PARAMETER_DELIMITER = ",";

    private static final String KEY_VALUE_DELIMITER = "=";

    private final Map<Integer, String> map;

    public ShardingItemParameters(final String shardingItemParameters) {
        map = toMap(shardingItemParameters);
    }
    
    /** 功能描述:  字符串转 Map
    *
    * @param: [originalShardingItemParameters]
    * @return: java.util.Map<java.lang.Integer,java.lang.String>
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/21 16:28
    */
    private Map<Integer, String> toMap(final String originalShardingItemParameters) {
        if (StringUtils.isNullOrEmpty(originalShardingItemParameters)) {
            return Collections.emptyMap();
        }
        String[] shardingItemParameters = originalShardingItemParameters.split(PARAMETER_DELIMITER);
        Map<Integer, String> result = new HashMap<>(shardingItemParameters.length);
        for (String each : shardingItemParameters) {
            ShardingItem shardingItem = parse(each, originalShardingItemParameters);
            result.put(shardingItem.item, shardingItem.parameter);
        }
        return result;
    }

    private ShardingItem parse(final String shardingItemParameter, final String originalShardingItemParameters) {
        String[] pair = shardingItemParameter.trim().split(KEY_VALUE_DELIMITER);
        if (2 != pair.length) {
            throw new JobConfigurationException("Sharding item parameters '%s' format error, should be int=xx,int=xx", originalShardingItemParameters);
        }
        try {
            return new ShardingItem(Integer.parseInt(pair[0].trim()), pair[1].trim());
        } catch (final NumberFormatException ex) {
            throw new JobConfigurationException("Sharding item parameters key '%s' is not an integer.", pair[0]);
        }
    }

    /**
     * Sharding item.
     */
    private static final class ShardingItem {
        private final int item;
        private final String parameter;

        public ShardingItem(int item, String parameter) {
            this.item = item;
            this.parameter = parameter;
        }
    }

    public static String getParameterDelimiter() {
        return PARAMETER_DELIMITER;
    }

    public static String getKeyValueDelimiter() {
        return KEY_VALUE_DELIMITER;
    }

    public Map<Integer, String> getMap() {
        return map;
    }
}
