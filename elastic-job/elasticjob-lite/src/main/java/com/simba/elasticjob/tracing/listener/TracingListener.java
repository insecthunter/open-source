package com.simba.elasticjob.tracing.listener;

import com.simba.elasticjob.event.JobExecutionEvent;
import com.simba.elasticjob.event.JobStatusTraceEvent;

/**
 * @Description Tracing listener.
 * @Author yuanjx3
 * @Date 2021/1/21 19:37
 * @Version V1.0
 **/
public interface TracingListener {
    /**
     * Listen job execution event.
     *
     * @param jobExecutionEvent job execution event
     */
    void listen(JobExecutionEvent jobExecutionEvent);

    /**
     * Listen job status trace event.
     *
     * @param jobStatusTraceEvent job status trace event
     */
    void listen(JobStatusTraceEvent jobStatusTraceEvent);
}
