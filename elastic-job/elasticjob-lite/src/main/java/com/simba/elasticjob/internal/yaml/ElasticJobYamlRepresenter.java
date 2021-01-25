package com.simba.elasticjob.internal.yaml;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/** 功能描述:
* @Author: yuanjx3
* @Date: 2021/1/15 9:58
*/
public class ElasticJobYamlRepresenter extends Representer{
    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        return new DefaultYamlTupleProcessor().process(super.representJavaBeanProperty(javaBean, property, propertyValue, customTag));
    }
}