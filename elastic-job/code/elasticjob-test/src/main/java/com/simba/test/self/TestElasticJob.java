package com.simba.test.self;

import com.simba.elasticjob.boostrap.impl.ScheduleJobBootstrap;
import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.configuration.ZookeeperConfiguration;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.register.zookeeper.ZookeeperRegistryCenter;


/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/24 10:40
 * @Version V1.0
 **/
public class TestElasticJob {
    public static void main(String[] args) {
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration()).schedule();
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("192.168.56.150:2181", "my-job"));
        regCenter.init();
        return regCenter;
    }

    private static JobConfiguration createJobConfiguration() {
        // 创建作业配置
        JobConfiguration jobConfig = JobConfiguration.newBuilder("MyJob", 3)
                .cron("0/5 * * * * ?")
                .build();
        return jobConfig;
    }
}
