package com.simba.test.self;


import com.simba.elasticjob.api.ShardingContext;
import com.simba.elasticjob.executor.item.simple.SimpleJob;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/13 18:54
 * @Version V1.0
 **/
public class MyJob implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        switch (shardingContext.getShardingItem()) {
            case 0:
                // do something by sharding item 0

                System.out.println("ShardingItem 0: " + shardingContext.toString());
                break;
            case 1:
                // do something by sharding item 1
                System.out.println("ShardingItem 1: " + shardingContext.toString());
                break;
            case 2:
                // do something by sharding item 2
                System.out.println("ShardingItem 2:" + shardingContext.toString());
                break;
            // case n: ...
        }
    }
}
