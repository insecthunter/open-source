package com.simba.elasticjob.internal.listener;

import com.simba.elasticjob.internal.storage.JobNodeStorage;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 监听器管理公共抽象类
 * @Author yuanjx3
 * @Date 2021/1/19 10:23
 * @Version V1.0
 **/
public abstract class AbstractListenerManager {
    public Logger log = LoggerFactory.getLogger(getClass());
    private final JobNodeStorage jobNodeStorage;

    protected AbstractListenerManager(CoordinatorRegistryCenter registryCenter, String jobName){
        jobNodeStorage = new JobNodeStorage(registryCenter, jobName);
    }
    
    /** 功能描述:  启动监听器
    * @Author: yuanjx3
    * @Date: 2021/1/19 10:27
    */
    public abstract void start();

    /** 功能描述: 添加数据监听器
    * @Author: yuanjx3
    * @Date: 2021/1/19 10:27
    */
    protected  void addDataListener(CuratorCacheListener listener){
        log.debug("add Data Listener: " + listener);
        jobNodeStorage.addDataListener(listener);
    }
}
