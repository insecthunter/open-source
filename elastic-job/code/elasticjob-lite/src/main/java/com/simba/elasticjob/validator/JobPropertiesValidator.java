package com.simba.elasticjob.validator;

import com.simba.elasticjob.spi.TypedSPI;

import java.util.Properties;

/**
 * @Description  Job properties validator. 作业属性验证器
 * @Author yuanjx3
 * @Date 2021/1/21 15:42
 * @Version V1.0
 **/
public interface JobPropertiesValidator extends TypedSPI {

    /**
     * Validate job properties.
     *
     * @param props job properties
     */
    void validate(Properties props);
}
