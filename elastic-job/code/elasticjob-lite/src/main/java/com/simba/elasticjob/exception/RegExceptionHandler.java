package com.simba.elasticjob.exception;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 注册中心异常处理类
 * @Author yuanjx3
 * @Date 2021/1/12 15:12
 * @Version V1.0
 **/
public class RegExceptionHandler {
    protected final static Logger logger = LoggerFactory.getLogger(RegExceptionHandler.class);

    public static void handleException(final Exception e){
        if (null==e) return;
        // 如果是由可忽略的异常抛出的Exception，或者是由可忽略的异常引起的，则打印异常信息
        if (isIgnoredException(e) || null!=e.getCause() && isIgnoredException(e.getCause())){
            logger.debug("Simba Elastic job: ignored exception for: {}", e.getMessage());
        }else if (e instanceof InterruptedException){
            Thread.currentThread().interrupt();     // 如果是线程打断异常，则将当前线程中断
        }else{
            throw new RegException(e);              //否则抛出一个自定义异常
        }
    }

    /** 功能描述: 如果异常类型是 zookeeper的ConnectionLossException、NoNodeException、NodeExistsException中的某一类型的话，返回true
    * @return: boolean
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/12 15:25
    */
    private static boolean isIgnoredException(final Throwable e) {
        return e instanceof KeeperException.ConnectionLossException || e instanceof KeeperException.NoNodeException || e instanceof KeeperException.NodeExistsException;
    }


}
