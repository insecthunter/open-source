package com.simba.elasticjob.executor.item.custom;

import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.repository.BarRepository;
import com.simba.elasticjob.repository.impl.BarRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 用户自定义作业测试
 * @Author yuanjx3
 * @Date 2021/1/22 14:28
 * @Version V1.0
 **/
public class CustomTestJob implements CustomJob {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BarRepository barRepository = new BarRepositoryImpl();

    @Override
    public void execute(final ShardingContext shardingContext) {
        int i = shardingContext.getShardingItem();
        List<String> results = new ArrayList<>();
        String data;
        while (null != (data = barRepository.getById(i))) {
            results.add(data);
            i += shardingContext.getShardingTotalCount();
        }
        log.info("{}", results);
    }
}
