package com.simba;

import com.simba.elasticjob.configuration.ZookeeperConfiguration;
import com.simba.elasticjob.internal.election.LeaderService;
import com.simba.elasticjob.register.zookeeper.ZookeeperRegistryCenter;

import java.lang.management.ManagementFactory;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 13:23
 * @Version V1.0
 **/
public class Test {
    public static void main(String[] args) {
        //获取当前正在运行的JVM的name
        //System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("localhost:2181", "simba-elastic-job-namespace");
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zookeeperConfiguration.setDigest("digest");
        ZookeeperRegistryCenter zkCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        String jobName = "test-JobName-110";
        LeaderService leaderService = new LeaderService(zkCenter,jobName);
        leaderService.electLeader();
    }
}
