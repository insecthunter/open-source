package com.simba.elasticjob.internal.server;


import java.util.Objects;
import java.util.regex.Pattern;

import com.simba.elasticjob.handler.sharding.JobInstance;
import com.simba.elasticjob.internal.schedule.JobRegistry;
import com.simba.elasticjob.internal.storage.*;
import com.simba.elasticjob.utils.IpUtils;

/**
 * @Description 服务器节点类
 * @Author yuanjx3
 * @Date 2021/1/15 15:49
 * @Version V1.0
 **/
public class ServerNode {
    public static String ROOT = "servers";
    private String SERVERS = ROOT + "/%s";
    private String jobName;
    private JobNodePath jobNodePath;

    public ServerNode(String jobName){
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }

    /** 功能描述: 判断传入的内容是否为服务器地址
    * @Author: yuanjx3
    * @Date: 2021/1/15 15:51
    */
    public boolean isServerPath(String path){
        return Pattern.compile(jobNodePath.getFullPath(ServerNode.ROOT) + "/"+ IpUtils.IP_REGEX).matcher(path).matches();
    }

    /** 功能描述:   判断是否为本地服务器地址
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:06
    */
    public boolean isLocalServerPath(String path){
        JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
        if (Objects.isNull(jobInstance)){
            return false;
        }
        return path.equals(jobNodePath.getFullPath(String.format(SERVERS, jobInstance.getIp())));
    }

    String getServerNode(String ip){
        return String.format(SERVERS,ip);
    }
}






