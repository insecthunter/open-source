package com.simba.elasticjob.internal.yaml;

import org.yaml.snakeyaml.Yaml;

/**
 * @Description Yaml 编排引擎
 *      elasticjob 在这里使用了snakeyaml插件来进行yaml的编排、解编操作，我这里照搬过来
 * @Author yuanjx3
 * @Date 2021/1/15 9:47
 * @Version V1.0
 **/
public class YamlEngine {

    /** 功能描述: 将数据编排为Yaml字符串返回
    * @Author: yuanjx3
    * @Date: 2021/1/15 9:48
    */
    public static String marshal(Object value){
        return new Yaml(new ElasticJobYamlRepresenter()).dumpAsMap(value);
    }

    /** 功能描述: 将yaml格式的字符串数据解编返回
     * @Author: yuanjx3
     * @Date: 2021/1/15 9:48
     */
    public static <T> T unmarshal(String yamlString, Class<T> classType){
        return new Yaml().loadAs(yamlString, classType);
    }
}
