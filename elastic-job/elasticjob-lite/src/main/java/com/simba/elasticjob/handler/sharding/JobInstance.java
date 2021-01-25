package com.simba.elasticjob.handler.sharding;

import com.simba.elasticjob.utils.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * @Description 作业运行实例
 * @Author yuanjx3
 * @Date 2021/1/15 17:02
 * @Version V1.0
 **/
public class JobInstance {
    private static final String DELIMITER = "@-@";
    private final String jobInstanceId;

    public JobInstance(String jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }
    public JobInstance() {
        //ManagementFactory.getRuntimeMXBean().getName(): 获取当前运行的JVM的虚拟机名称
        this.jobInstanceId = IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }
    public String getIp(){
        return this.jobInstanceId.substring(0,jobInstanceId.indexOf(DELIMITER));
    }

    public static String getDELIMITER() {
        return DELIMITER;
    }

    public String getJobInstanceId() {
        return jobInstanceId;
    }
}

