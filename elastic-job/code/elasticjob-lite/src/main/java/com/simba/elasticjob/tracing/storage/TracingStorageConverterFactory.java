package com.simba.elasticjob.tracing.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 19:26
 * @Version V1.0
 **/
public class TracingStorageConverterFactory {
    private TracingStorageConverterFactory() {}
    private static final List<TracingStorageConverter<?>> CONVERTERS = new LinkedList<>();

    static {
        ServiceLoader.load(TracingStorageConverter.class).forEach(CONVERTERS::add);
    }

    /**
     * Find {@link TracingStorageConverter} for specific storage type.
     *
     * @param storageType storage type
     * @param <T>         storage type
     * @return instance of {@link TracingStorageConverter}
     */
    public static <T> Optional<TracingStorageConverter<T>> findConverter(final Class<T> storageType) {
        return CONVERTERS.stream().filter(each -> each.storageType().isAssignableFrom(storageType)).map(each -> (TracingStorageConverter<T>) each).findFirst();
    }
}
