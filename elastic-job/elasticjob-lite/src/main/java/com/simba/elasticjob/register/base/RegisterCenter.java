package com.simba.elasticjob.register.base;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/14 19:35
 * @Version V1.0
 **/
public interface RegisterCenter {
    /** 初始化注册中心 **/
    void init();

    /** 关闭注册中心 **/
    void close();

    /** 从注册中心通过key获取value **/
    String get(String key);

    /** 判断key是否存在 **/
    boolean isExisted(String key);

    /** 创建节点 **/
    void persist(String key, String value);

    /** 更新key-value **/
    void update(String key, String value);

    /** 根据key删除对应数据 **/
    void remove(String key);

    /** 获取注册中心当前时间 **/
    long getRegistryCenterTime(String key);

    /** 通过注册中心客户端获取一个原始的客户端操作句柄 **/
    Object getRawClient();


}
