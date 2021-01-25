package com.simba.elasticjob.register.zookeeper;

import com.simba.elasticjob.configuration.ZookeeperConfiguration;
import com.simba.elasticjob.exception.RegExceptionHandler;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import com.simba.elasticjob.utils.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.io.Closeables;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description zookeeper注册中心
 * @Author yuanjx3
 * @Date 2021/1/12 14:06
 * @Version V1.0
 **/
public final class ZookeeperRegistryCenter implements CoordinatorRegistryCenter {

    /** 功能描述:  zookeeper 的节点信息说明
     * cZxid				创建该节点的 zxid（zxid叫做事务id。zookeeper中的每一次数据更新都会有一个事务，zxid就是事务的唯一表示）
     *
     *   mZxid				最后修改该节点的 zxid
     *   pZxid				znode 最后更新的子节点 zxid
     *   ctime				该节点的创建时间
     *   mtime				该节点的最后修改时间
     *   dataVersion		数据版本号，该节点数据被修改的次数
     *   cversion			该节点的子节点变更次数
     *   ephemeralOwner	    临时节点的所有者会话 id，如果不是临时节点，则为0
     *   dataLength		    该节点的数据长度
     *   numChildren		子节点数
    *
    * @Date: 2021/1/12 16:53
    */
    private final ZookeeperConfiguration zkConfiguration;

    /**
     * 在使用原生的ZooKeeper的时候，是可以使用Watcher对节点进行监听的，
     * 但是一个Watcher只能生效一次，即每次进行监听回调之后我们需要自
     * 己重新的设置监听才能达到永久监听的效果。
     *
     * CuratorCache是Curator对事件监听的包装，其对事件的监听可以近似看
     * 做是一个本地缓存视图和远程ZooKeeper视图的对比过程。而且Curator会
     * 自动的再次监听，我们就不需要自己手动的重复监听了。
    *
    */
    private final Map<String, CuratorCache> caches = new ConcurrentHashMap<>();
    private CuratorFramework zkClient;

    public ZookeeperRegistryCenter(ZookeeperConfiguration zookeeperConfiguration) {
        this.zkConfiguration = zookeeperConfiguration;
    }

    /** 功能描述: 注册中心初始化方法
     *  完成zookeeper客户端连接的建立等功能
    * @Author: yuanjx3
    * @Date: 2021/1/12 15:28
    */
    public void init(){
        log.debug("Simba 注册中心初始化开始, server lists is {}.",
                zkConfiguration.getServerLists());
        //1.初始化zookeeper客户端builder
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zkConfiguration.getServerLists())    //zookeeper集群地址
                .retryPolicy(new ExponentialBackoffRetry(           //重试策略
                        zkConfiguration.getBaseSleepTimeMilliseconds(), //重试最少等待时间
                        zkConfiguration.getMaxRetries(),                //重试次数
                        zkConfiguration.getMaxSleepTimeMilliseconds())) //重试最大等待时间
                .namespace(zkConfiguration.getNamespace());             //命名空间
        //设置session超时时间
        if (0!=zkConfiguration.getSessionTimeoutMilliseconds()){
            builder.sessionTimeoutMs(zkConfiguration.getSessionTimeoutMilliseconds());
        }
        //设置连接超时时间
        if (0!=zkConfiguration.getConnectionTimeoutMilliseconds()){
            builder.connectionTimeoutMs(zkConfiguration.getConnectionTimeoutMilliseconds());
        }
        log.debug("zkConfiguration.getDigest(): " + zkConfiguration.getDigest() + ",isNull: " + StringUtils.isNullOrEmpty(zkConfiguration.getDigest()));
        if (!StringUtils.isNullOrEmpty(zkConfiguration.getDigest())){
            builder.authorization("digest",zkConfiguration.getDigest().getBytes(StandardCharsets.UTF_8))
                    //设置zookeeper的ACL（Access Control）
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(String s) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        zkClient = builder.build();
        zkClient.start();
        try{
            // 如果重试次数达到设置的MaxRetries时依然没有连接上，则关闭zookeeper客户端，抛出操作超时异常信息
            if (!zkClient.blockUntilConnected(zkConfiguration.getMaxSleepTimeMilliseconds()*zkConfiguration.getMaxRetries(), TimeUnit.MILLISECONDS)){
                zkClient.close();
                log.debug("zkClient closed!");
                throw new KeeperException.OperationTimeoutException();
            }
        }catch (final Exception e){
            RegExceptionHandler.handleException(e); //异常信息交由异常处理类处理
        }
        log.debug("注册中心 ZookeeperRegistryCenter 初始化完成~~~~~~~~~~"+ zkClient);
    }
    
    /** 功能描述: 关闭注册中心，将建立的zookeeper客户端连接全部关闭
    * @Author: yuanjx3
    * @Date: 2021/1/12 15:29
    */
    public void close(){
        for (Map.Entry<String, CuratorCache> each: caches.entrySet()){
            each.getValue().close();
        }
        //等待500毫秒，让缓存中的zookeeper客户端连接全部关闭
        waitForCacheClose();
        //关闭zookeeper客户端连接
        closeQuietly(zkClient);
    }

    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeQuietly(Closeable closeable){
        try {
            Closeables.close(closeable,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(final String key){
        log.debug(">>>>>into get method  " + key);
        CuratorCache cache = findCuratorCache(key);
        log.debug(">>>>>cache  " + cache);
        if (null==cache){
            return getDirectly(key);
        }
        Optional<ChildData> childData = cache.get(key);
        if (childData.isPresent()){
            return null==childData.get().getData()?null:new String(childData.get().getData(),StandardCharsets.UTF_8);
        }
        return getDirectly(key);
    }

    private CuratorCache findCuratorCache(final String key) {
        for (Map.Entry<String,CuratorCache> entry:caches.entrySet()){
            if (key.startsWith(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    public String getDirectly(String key) {
        try {
            return new String(zkClient.getData().forPath(key),StandardCharsets.UTF_8);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return null;
    }

    public List<String> getChildrenKeys(final String key){
        try {
            List<String> results = zkClient.getChildren().forPath(key);
            results.sort(Comparator.reverseOrder());
            return results;
        }catch (Exception e){
            RegExceptionHandler.handleException(e);
        }
        return Collections.emptyList();
    }

    @Override
    public int getNumChildren(String key) {
        try {
            Stat stat = zkClient.checkExists().forPath(key);
            if (null!=stat) return stat.getNumChildren();
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return 0;
    }

    public boolean isExisted(final String key){
        try {
            log.debug(">>> judge is  path Exist: " + key);
            boolean isExist = null != zkClient.checkExists().forPath(key);
            log.debug(">>>isExist: " + isExist);
            return isExist;
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return false;
    }

    /** 功能描述: 创建或更新zk的持久node节点
    * @Author: yuanjx3
    * @Date: 2021/1/12 16:32
    */
    public void persist(String key, String value){
        log.debug(">>>>>into persist method : " + key+ ", " + value);
        try {
            if (!isExisted(key)){
                String s = zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
                log.debug(">>>>>persist result : " + s);
            }else {
                log.debug(">>>>>to update");
                update(key, value);
            }
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    public void update(String key, String value){
        try {
            TransactionOp transactionOp = zkClient.transactionOp();
            zkClient.transaction()
                    .forOperations(
                            transactionOp.check().forPath(key),
                            transactionOp.setData().forPath(key,value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /** 功能描述: 创建zk的临时node节点
    * @Author: yuanjx3
    * @Date: 2021/1/12 16:37
    */
    public void persistEphemeral(final String key, final String value){
        try {
            if(isExisted(key)){
                zkClient.delete().deletingChildrenIfNeeded().forPath(key);
            }
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key,value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /** 功能描述:   创建持久顺序node节点
    * @Author: yuanjx3
    * @Date: 2021/1/12 16:41
    */
    public String persistSequential(final String key, final String value){
        try {
            return zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key,value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return null;
    }

    /** 功能描述:   创建临时顺序node节点
     * @Author: yuanjx3
     * @Date: 2021/1/12 16:41
     */
    public void persistEphemeralSequential(final String key){
        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /** 功能描述:   删除节点
    * @Author: yuanjx3
    * @Date: 2021/1/12 16:45
    */
    public void remove(final String key){
        try {
            zkClient.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /** 功能描述:   获取注册中心的时间
    * @Author: yuanjx3
    * @Date: 2021/1/12 16:46
    */
    public long getRegistryCenterTime(final String key){
        long time = 0L;
        try {
            //先在zk中创建一个value为空的节点，然后获取这个节点的最后修改时间（因为如果要创建的节点存在的话，
            // 只是更新该节点，所以如果获取创建节点的时间时会不准确）
            persist(key,"");
            time = zkClient.checkExists().forPath(key).getMtime();
            zkClient.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        checkState(0L!=time,"Cann't get registry center time.");
        return time;
    }

    /** 功能描述:   获取zk客户端
     * @Author: yuanjx3
     * @Date: 2021/1/12 16:56
     */
    @Override
    public Object getRawClient() {
        return zkClient;
    }

    private void checkState(boolean expression, String errorMessage){
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public void addCacheData(String cachePath){
        CuratorCache cache = CuratorCache.build(zkClient, cachePath);
        cache.start();
        caches.put(cachePath+"/",cache);
    }

    /** 功能描述:   移除指定地址的zk缓存
    * @Author: yuanjx3
    * @Date: 2021/1/12 17:01
    */
    public void evictCacheData(String cachePath){
        CuratorCache removedCache = caches.remove(cachePath + "/");
        if (null!=removedCache){
            removedCache.close();
        }
    }

    public Object getRawCache(String cachePath){
        return caches.get(cachePath+"/");
    }

    public static void main(String[] args) {
        /*ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("localhost:2181", "simba-elastic-job-namespace");
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(30000);
        zookeeperConfiguration.setDigest("digest");
        ZookeeperRegistryCenter zkCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zkCenter.init();
        zkCenter.persist("/simba", "test");
        zkCenter.persist("/simba/deep/nested", "deepNested");
        zkCenter.persist("/simba/child", "child");

        log.debug(zkCenter.get("/simba"));
        log.debug(zkCenter.get("/simba/deep/nested"));
        log.debug(zkCenter.get("/simba/child"));*/
    }

}













