package com.simba.elasticjob.internal.storage;

import com.simba.elasticjob.exception.JobSystemException;
import com.simba.elasticjob.exception.RegExceptionHandler;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description 任务节点存储类
 * @Author yuanjx3
 * @Date 2021/1/14 20:09
 * @Version V1.0
 **/
public class JobNodeStorage {
    private final CoordinatorRegistryCenter registryCenter;
    private final String jobName;
    private final JobNodePath jobNodePath;

    public JobNodeStorage(CoordinatorRegistryCenter registryCenter, String jobName) {
        this.registryCenter = registryCenter;
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }
    
    /** 功能描述:   判断任务节点是否存在
    * @Author: yuanjx3
    * @Date: 2021/1/14 20:26
    */
    public boolean isJobNodeExisted(String nodeName){
        return registryCenter.isExisted(jobNodePath.getFullPath(nodeName));
    }

    /** 功能描述:   判断任务根节点是否存在
     * @Author: yuanjx3
     * @Date: 2021/1/14 20:26
     */
    public boolean isJobRootNodeExisted(){
        return registryCenter.isExisted("/"+jobName);
    }

    /** 功能描述:   获取job节点数据
     * @Author: yuanjx3
     * @Date: 2021/1/14 20:26
     */
    public String getJobNodeData(String nodeName){
        return registryCenter.get(jobNodePath.getFullPath(nodeName));
    }

    /** 功能描述:   直接从注册中心获取job节点数据
     * @Author: yuanjx3
     * @Date: 2021/1/14 20:26
     */
    public String getJobNodeDataDirectly(String nodeName){
        return registryCenter.getDirectly(jobNodePath.getFullPath(nodeName));
    }

    /** 功能描述:   获取job节点的所有子节点的 key 集合
     * @Author: yuanjx3
     * @Date: 2021/1/14 20:26
     */
    public List<String> getChildrenKeys(String nodeName){
        return registryCenter.getChildrenKeys(jobNodePath.getFullPath(nodeName));
    }

    /** 功能描述:   获取job根节点的数据
     * @Author: yuanjx3
     * @Date: 2021/1/14 20:26
     */
    public String getJobRootNodeData(){
        return registryCenter.get("/"+jobName);
    }

    /** 如果根节点存在，但是需要的节点不存在，则创建 （注意：根节点不存在时不要创建节点，因为job可能已经被终止了）*/
    public void createJobNodeIfNeeded(final String nodeName) {
        if (isJobRootNodeExisted() && !isJobNodeExisted(nodeName)) {
            registryCenter.persist(jobNodePath.getFullPath(nodeName), "");
        }
    }

    /** 如果节点存在则删除该节点 */
    public void removeJobNodeIfExisted(final String nodeName) {
        if (isJobNodeExisted(nodeName)) {
            registryCenter.remove(jobNodePath.getFullPath(nodeName));
        }
    }

    /** 创建节点 */
    public void fillJobNode(final String nodeName, final Object value) {
        registryCenter.persist(jobNodePath.getFullPath(nodeName), value.toString());
    }

    /** 创建临时节点 */
    public void fillEphemeralJobNode(final String nodeName, final Object value) {
        registryCenter.persistEphemeral(jobNodePath.getFullPath(nodeName), value.toString());
    }

    /** 更新job节点 */
    public void updateJobNode(final String nodeName, final Object value) {
        registryCenter.update(jobNodePath.getFullPath(nodeName), value.toString());
    }

    /** 覆盖job节点数据 */
    public void replaceJobNode(final String nodeName, final Object value) {
        registryCenter.persist(jobNodePath.getFullPath(nodeName), value.toString());
    }

    /** 覆盖根节点数据 */
    public void replaceJobRootNode(final Object value) {
        registryCenter.persist("/" + jobName, value.toString());
    }

    /** 在事务中执行操作 */
    public void executeInTransaction(final TransactionExecutionCallback callback) {
        try {
            List<CuratorOp> operations = new LinkedList<>();
            CuratorFramework client = getClient();
            TransactionOp transactionOp = client.transactionOp();
            operations.add(transactionOp.check().forPath("/"));
            operations.addAll(callback.createCuratorOperators(transactionOp));
            client.transaction().forOperations(operations);
            //CHECKSTYLE:OFF
        } catch (final Exception e) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(e);
        }
    }

    /**
     * 执行Leader选举: 在leader 主机上执行的方法
     * @param latchPath Leader选举根节点路径
     * @param callback leader选举结束的回调方法
     */
    public void executeInLeader(final String latchPath, final LeaderExecutionCallback callback) {
        try (LeaderLatch latch = new LeaderLatch(getClient(), jobNodePath.getFullPath(latchPath))) {

            /**
             * 调用start方法开始抢主：
             * 这里通过调用start()方法启动向leader主机的根路径 latchPath 下创建临时顺序节点,
             * 如"/leader_latch/node_1"，节点编号最小(这里为node_1)的zk客户端成为leader，没抢到Leader的节点都监听前一
             * 个节点的删除事件，在前一个节点删除后进行重新抢主。
             */
            latch.start();
            //await方法阻塞线程，获取leader权限后唤醒继续执行
            latch.await();
            //抢主成功后执行回调操作
            callback.execute();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            handleException(ex);
        }
    }

    private void handleException(final Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new JobSystemException(ex);
        }
    }

    /**
     * 给注册中心客户端添加连接状态监听器
     * @param listener 连接状态监听器
     */
    public void addConnectionStateListener(final ConnectionStateListener listener) {
        getClient().getConnectionStateListenable().addListener(listener);
    }

    private CuratorFramework getClient() {
        return (CuratorFramework) registryCenter.getRawClient();
    }

    /**
     * 通过CuratorCache给指定节点添加数据监听器
     * @param listener：数据监听器
     */
    public void addDataListener(final CuratorCacheListener listener) {
        CuratorCache cache = (CuratorCache) registryCenter.getRawCache("/" + jobName);
        cache.listenable().addListener(listener);
    }

    /** 获取注册中心当前时间 */
    public long getRegistryCenterTime() {
        return registryCenter.getRegistryCenterTime(jobNodePath.getFullPath("systemTime/current"));
    }
}
