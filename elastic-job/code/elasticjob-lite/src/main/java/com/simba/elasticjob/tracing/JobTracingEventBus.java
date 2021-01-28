package com.simba.elasticjob.tracing;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import com.simba.elasticjob.event.JobEvent;
import com.simba.elasticjob.tracing.api.TracingConfiguration;
import com.simba.elasticjob.tracing.exception.TracingConfigurationException;
import com.simba.elasticjob.tracing.listener.TracingListenerFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description 作业追踪事件总线
 * @Author yuanjx3
 * @Date 2021/1/21 19:21
 * @Version V1.0
 **/
public final class JobTracingEventBus {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService;

    private final EventBus eventBus;

    private volatile boolean isRegistered;

    public JobTracingEventBus() {
        executorService = null;
        eventBus = null;
    }

    public JobTracingEventBus(final TracingConfiguration<?> tracingConfig) {
        executorService = createExecutorService(Runtime.getRuntime().availableProcessors() * 2);
        eventBus = new AsyncEventBus(executorService);
        register(tracingConfig);
    }

    private ExecutorService createExecutorService(final int threadSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize, 5L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(), new BasicThreadFactory.Builder().namingPattern(String.join("-", "job-event", "%s")).build());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return MoreExecutors.listeningDecorator(MoreExecutors.getExitingExecutorService(threadPoolExecutor));
    }

    private void register(final TracingConfiguration<?> tracingConfig) {
        log.debug("JobTracingEventBus register： " + tracingConfig);
        try {
            eventBus.register(TracingListenerFactory.getListener(tracingConfig));
            isRegistered = true;
        } catch (final TracingConfigurationException ex) {
            log.error("Elastic job: create tracing listener failure, error is: ", ex);
        }
    }

    /**
     * Post event.
     * @param event job event
     */
    public void post(final JobEvent event) {
        log.debug("JobTracingEventBus Post event： " + event);
        if (isRegistered && !executorService.isShutdown()) {
            eventBus.post(event);
        }
    }
}
