package com.simba.elasticjob.internal.config;

import com.simba.elasticjob.internal.storage.JobNodePath;

/**
 * @Description 配置信息节点
 * @Author yuanjx3
 * @Date 2021/1/15 9:31
 * @Version V1.0
 **/
public class ConfigurationNode {
    static final String ROOT = "config";
    private final JobNodePath jobNodePath;

    public ConfigurationNode(String jobName) {
        this.jobNodePath = new JobNodePath(jobName);
    }

    /** 功能描述:  判断传入的路径是否为配置信息的根节点
    * @Author: yuanjx3
    * @Date: 2021/1/15 9:42
    */
    public boolean isConfigPath(String path){
        return jobNodePath.getConfigNodePath().equals(path);
    }
}
