package com.simba.elasticjob.internal.storage;

import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;

import java.util.List;

/**
 * @Description 事务执行回调接口类
 * @Author yuanjx3
 * @Date 2021/1/14 20:45
 * @Version V1.0
 **/
public interface TransactionExecutionCallback {

    /** 功能描述: 创建 Curator 的 实务操作集合
    * @Author: yuanjx3
    * @Date: 2021/1/14 20:47
    */
    List<CuratorOp> createCuratorOperators(TransactionOp transactionOp) throws Exception;
}
