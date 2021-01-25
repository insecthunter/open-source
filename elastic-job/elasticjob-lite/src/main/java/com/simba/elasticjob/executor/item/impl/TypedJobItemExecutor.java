package com.simba.elasticjob.executor.item.impl;

import com.simba.elasticjob.api.ElasticJob;
import com.simba.elasticjob.executor.item.JobItemExecutor;
import com.simba.elasticjob.spi.TypedSPI;

/**
 * Typed job item executor. 字符串标记类型的作业项目执行器
 */
public interface TypedJobItemExecutor extends JobItemExecutor<ElasticJob>, TypedSPI {
}
