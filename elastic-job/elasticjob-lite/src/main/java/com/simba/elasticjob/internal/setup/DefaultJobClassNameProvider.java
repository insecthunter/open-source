package com.simba.elasticjob.internal.setup;

import com.simba.elasticjob.api.ElasticJob;

/**
 * Simple job class name provider.
 */
public final class DefaultJobClassNameProvider implements JobClassNameProvider {
    
    private static final String LAMBDA_CHARACTERISTICS = "$$Lambda$";
    
    @Override
    public String getJobClassName(final ElasticJob elasticJob) {
        Class<? extends ElasticJob> elasticJobClass = elasticJob.getClass();
        String elasticJobClassName = elasticJobClass.getName();
        return isLambdaClass(elasticJobClass) ? trimLambdaClassSuffix(elasticJobClassName) : elasticJobClassName;
    }
    
    private boolean isLambdaClass(final Class<? extends ElasticJob> elasticJobClass) {
        return elasticJobClass.isSynthetic() && elasticJobClass.getSimpleName().contains(LAMBDA_CHARACTERISTICS);
    }
    
    private String trimLambdaClassSuffix(final String className) {
        return className.substring(0, className.lastIndexOf(LAMBDA_CHARACTERISTICS) + LAMBDA_CHARACTERISTICS.length());
    }
}
