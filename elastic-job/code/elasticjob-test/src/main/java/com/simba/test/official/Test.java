//package com.simba.test.official;
//
//
//import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
//import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
//import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
//import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
//import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
//
///**
// * @Description
// * @Author yuanjx3
// * @Date 2021/1/13 19:03
// * @Version V1.0
// **/
//public class Test {
//    public static void main(String[] args) {
//        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration()).schedule();
//    }
//
//    private static CoordinatorRegistryCenter createRegistryCenter() {
//        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("localhost:2181", "my-job"));
//        regCenter.init();
//        return regCenter;
//    }
//
//    private static JobConfiguration createJobConfiguration() {
//        // 创建作业配置
//        JobConfiguration jobConfig = JobConfiguration.newBuilder("MyJob", 3)
//                                                     .cron("0/5 * * * * ?")
//                                                     .build();
//        return jobConfig;
//    }
//}
