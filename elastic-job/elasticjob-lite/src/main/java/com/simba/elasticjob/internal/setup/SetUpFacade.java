package com.simba.elasticjob.internal.setup;

import com.simba.elasticjob.configuration.JobConfiguration;
import com.simba.elasticjob.internal.config.ConfigurationService;
import com.simba.elasticjob.internal.election.LeaderService;
import com.simba.elasticjob.internal.instance.InstanceService;
import com.simba.elasticjob.internal.listener.ElasticJobListener;
import com.simba.elasticjob.internal.listener.ListenerManager;
import com.simba.elasticjob.internal.reconcile.ReconcileService;
import com.simba.elasticjob.internal.server.ServerService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @Description 设置代理操作类（门面类）
 * @Author yuanjx3
 * @Date 2021/1/14 20:03
 * @Version V1.0
 **/
public class SetUpFacade {
    Logger log = LoggerFactory.getLogger(getClass());
    private final ConfigurationService configService;
    private final LeaderService leaderService;
    private final ServerService serverService;
    private final InstanceService instanceService;
    private final ReconcileService reconcileService;
    private final ListenerManager listenerManager;

    public SetUpFacade(CoordinatorRegistryCenter registryCenter, String jobName, Collection<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(registryCenter, jobName);
        leaderService = new LeaderService(registryCenter, jobName);
        serverService = new ServerService(registryCenter, jobName);
        instanceService = new InstanceService(registryCenter, jobName);
        reconcileService = new ReconcileService(registryCenter, jobName);
        listenerManager = new ListenerManager(registryCenter, jobName, elasticJobListeners);
    }
    
    /** 功能描述:  设置作业配置
    * @param: [jobClassName] job class name
    * @param: [jobConfig] job configuration to be updated
    * @return: com.simba.elasticjob.configuration.JobConfiguration
    * @version: 1.0.0
    * @Author: yuanjx3
    * @Date: 2021/1/21 12:27
    */
    public JobConfiguration setUpJobConfiguration(String jobClassName, JobConfiguration jobConfig) {
        log.debug("设置作业配置, jobClassName: " + jobClassName + ",jobConfig: " + jobConfig);
        return configService.setUpJobConfiguration(jobClassName, jobConfig);
    }

    /** 功能描述:  注册启动信息
     * @param enabled enable job on startup
     * @version: 1.0.0
     * @Author: yuanjx3
     * @Date: 2021/1/21 12:27
     */
    public void registerStartUpInfo(boolean enabled) {
        log.debug("注册启动信息，这里依次执行：1.启动所有的监听器;2.启动leader选举;3.保存服务器节点的上线状态;4.保存作业在线状态;5.启动调度协调服务");
        // 1.启动所有的监听器
        listenerManager.startAllListeners();
        // 2.启动leader选举
        leaderService.electLeader();
        // 3.保存服务器节点的上线状态
        serverService.persistOnline(enabled);
        // 4.保存作业在线状态
        instanceService.persistOnline();
        // 5.启动调度协调服务
        if (!reconcileService.isRunning()){
            reconcileService.startAsync();
        }
    }
}
