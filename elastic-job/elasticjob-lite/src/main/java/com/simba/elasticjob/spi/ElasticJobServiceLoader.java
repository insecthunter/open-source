package com.simba.elasticjob.spi;

import com.simba.elasticjob.spi.exception.ServiceLoaderInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description ElasticJob service loader
 * @Author yuanjx3
 * @Date 2021/1/18 16:54
 * @Version V1.0
 **/
public class ElasticJobServiceLoader {
    private static final Logger log = LoggerFactory.getLogger(ElasticJobServiceLoader.class);
    public ElasticJobServiceLoader(){}

    private static final ConcurrentMap<Class<? extends TypedSPI>, ConcurrentMap<String, TypedSPI>> TYPED_SERVICES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<? extends TypedSPI>, ConcurrentMap<String, Class<? extends TypedSPI>>> TYPED_SERVICE_CLASSES = new ConcurrentHashMap<>();

    /** 功能描述: 注册 TypedSPI 服务
    * @Author: yuanjx3
    * @Date: 2021/1/18 17:00
    */
    public static <T extends TypedSPI> void registerTypedService(Class<T> typedService){
        log.debug("into registerTypedService method:: " + typedService);
        if (TYPED_SERVICES.containsKey(typedService)){
            return;
        }
        ServiceLoader.load(typedService).forEach(each->registerTypedServiceClass(typedService,each));
    }

    private static <T extends TypedSPI> void registerTypedServiceClass(Class<T> typedService, TypedSPI instance){
        log.debug("into registerTypedServiceClass method, typedService:" + typedService + ", instance: " + instance);
        TYPED_SERVICES.computeIfAbsent(typedService, unused->new ConcurrentHashMap<>())
                      .putIfAbsent(instance.getType(), instance);

        TYPED_SERVICE_CLASSES.computeIfAbsent(typedService, unused->new ConcurrentHashMap<>())
                             .putIfAbsent(instance.getType(), instance.getClass());
    }

    /** 功能描述: Get cached typed instance.
     * @Author: yuanjx3
     * @Date: 2021/1/18 17:00
     */
    public static <T extends TypedSPI> Optional<T> getCachedTypedServiceInstance(Class<T> typedServiceInterface, String type){
        return Optional.ofNullable(TYPED_SERVICES.get(typedServiceInterface))
                       .map(services->(T)services.get(type));
    }


    public static <T extends TypedSPI> Optional<T> newTypedServiceInstance(Class<T> typedServiceInterface, String type, Properties props){
        Optional<T> result = Optional.ofNullable(TYPED_SERVICE_CLASSES.get(typedServiceInterface))
                .map(serviceClasses -> serviceClasses.get(type))
                .map(clazz -> (T)newServiceInstance(clazz));
        if (result.isPresent() && result.get() instanceof SPIPostProcessor){
            ((SPIPostProcessor)result.get()).init(props);
        }
        return result;
    }

    private static Object newServiceInstance(Class<?> clazz){
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ServiceLoaderInstantiationException(clazz, e);
        }
    }
}
