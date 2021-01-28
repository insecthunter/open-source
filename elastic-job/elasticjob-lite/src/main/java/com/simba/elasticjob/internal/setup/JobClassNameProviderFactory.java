package com.simba.elasticjob.internal.setup;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @Description 作业类命提供工厂类
 * @Author yuanjx3
 * @Date 2021/1/22 9:37
 * @Version V1.0
 **/
public class JobClassNameProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(JobClassNameProviderFactory.class);

    private static final List<JobClassNameProvider> PROVIDERS = new LinkedList<>();
    private static final JobClassNameProvider DEFAULT_PROVIDER = new DefaultJobClassNameProvider();

    static {
        for (JobClassNameProvider each : ServiceLoader.load(JobClassNameProvider.class)) {
            log.debug("~~~initing JobClassNameProvider's implement class: " + each);
            PROVIDERS.add(each);
        }
    }

    /**
     * Get the first job class name provider.
     * @return job class name provider
     */
    public static JobClassNameProvider getProvider() {
        JobClassNameProvider jobClassNameProvider = PROVIDERS.isEmpty() ? DEFAULT_PROVIDER : PROVIDERS.get(0);
        log.debug("~~~getProvider result: " + jobClassNameProvider);
        return jobClassNameProvider;
    }
}
