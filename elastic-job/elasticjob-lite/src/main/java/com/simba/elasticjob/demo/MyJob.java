//package com.simba.elasticjob.demo;
//
//
//import org.apache.shardingsphere.elasticjob.api.ShardingContext;
//import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
//
///**
// * @Description
// * @Author yuanjx3
// * @Date 2021/1/13 18:54
// * @Version V1.0
// **/
//public class MyJob implements SimpleJob {
//    @Override
//    public void execute(ShardingContext shardingContext) {
//        switch (shardingContext.getShardingItem()) {
//            case 0:
//                // do something by sharding item 0
//                System.out.println("ShardingItem -----0");
//                break;
//            case 1:
//                // do something by sharding item 1
//                System.out.println("ShardingItem -----1");
//                break;
//            case 2:
//                // do something by sharding item 2
//                System.out.println("ShardingItem -----2");
//                break;
//            // case n: ...
//        }
//    }
//}
