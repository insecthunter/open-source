package com.simba.elasticjob.internal.yaml;

import java.io.Serializable;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/15 14:26
 * @Version V1.0
 **/
public interface YamlConfiguration<T>  extends Serializable {
    T toConfiguration();
}
