package com.simba.elasticjob.register.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description 协调器注册中心
 * @Author yuanjx3
 * @Date 2021/1/14 19:42
 * @Version V1.0
 **/
public interface CoordinatorRegistryCenter extends RegisterCenter {
    Logger log = LoggerFactory.getLogger(CoordinatorRegistryCenter.class);
    /** 通过key直接去注册中心查找value **/
    String getDirectly(String key);

    /** 查询key对应的所有子节点的key的集合 **/
    List<String> getChildrenKeys(String key);

    /** 查询key对应的所有子节点的个数 **/
    int getNumChildren(String key);

    /** 创建临时节点 **/
    void persistEphemeral(String key,String value);

    /** 创建顺序节点 **/
    String persistSequential(String key,String value);

    /** 创建临时顺序节点 **/
    void persistEphemeralSequential(String key);

    /** 添加节点监听 **/
    void addCacheData(String cachePath);

    /** 取消节点监听 **/
    void evictCacheData(String cachePath);

    /** 从注册中心获取节点监听对象 **/
    Object getRawCache(String cachePath);
}
