package com.simba.elasticjob.executor.context;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.executor.Reloadable;
import com.simba.elasticjob.executor.ReloadablePostProcessor;
import com.simba.elasticjob.spi.ElasticJobServiceLoader;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * @Description  Executor context.
 *  Reloadable接口实现类：
 *      @see  com.simba.elasticjob.error.handler.JobErrorHandlerReloadable
 *      @see  com.simba.elasticjob.concurrent.ExecutorServiceReloadable
 * @Author yuanjx3
 * @Date 2021/1/22 8:37
 * @Version V1.0
 **/
public final class ExecutorContext {
    static {
        ElasticJobServiceLoader.registerTypedService(Reloadable.class);
    }

    private final Map<String, Reloadable<?>> reloadableItems = new LinkedHashMap<>();

    public ExecutorContext(final JobConfiguration jobConfig) {
        /**
         *  加载所有Reloadable接口实现类，放入到reloadableItems集合中
         *  实现类有：
         *      com.simba.elasticjob.concurrent.ExecutorServiceReloadable
         *      com.simba.elasticjob.error.handler.JobErrorHandlerReloadable
         *  **/
        ServiceLoader.load(Reloadable.class).forEach(each -> {
            ElasticJobServiceLoader.newTypedServiceInstance(Reloadable.class, each.getType(), new Properties())
                    .ifPresent(reloadable -> reloadableItems.put(reloadable.getType(), reloadable));
        });
        initReloadable(jobConfig);
    }

    /** 初始化Reloadable **/
    private void initReloadable(final JobConfiguration jobConfig) {
        // 迭代reloadableItems集合，将所有类型为ReloadablePostProcessor的类进行初始化（即实例化：ExecutorServiceReloadable）
        reloadableItems.values().stream().filter(each -> each instanceof ReloadablePostProcessor).forEach(each -> ((ReloadablePostProcessor) each).init(jobConfig));
    }

    /**
     * Reload all reloadable item if necessary.
     *
     * @param jobConfiguration job configuration
     */
    public void reloadIfNecessary(final JobConfiguration jobConfiguration) {
        reloadableItems.values().forEach(each -> each.reloadIfNecessary(jobConfiguration));
    }

    /**
     * Get instance. 获取作业执行服务实例
     *
     * @param targetClass target class
     * @param <T>         target type
     * @return instance
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> targetClass) {
        return (T) reloadableItems.get(targetClass.getName()).getInstance();
    }

    /**
     * Shutdown all closeable instances.
     */
    public void shutdown() {
        for (Reloadable<?> each : reloadableItems.values()) {
            try {
                each.close();
            } catch (final IOException ignored) {
            }
        }
    }
}
