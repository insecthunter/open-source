package com.simba.elasticjob.spi;

import java.util.Properties;

/**
 * @Description SPI post processor.
 * @Author yuanjx3
 * @Date 2021/1/18 17:39
 * @Version V1.0
 **/
public interface SPIPostProcessor {

    /** 功能描述: 初始化 SPI 实例
    * @Author: yuanjx3
    * @Date: 2021/1/18 17:40
    */
    void init(Properties props);
}
