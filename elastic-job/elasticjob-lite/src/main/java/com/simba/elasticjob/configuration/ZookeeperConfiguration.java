package com.simba.elasticjob.configuration;

/**
 * @Description zookeeper 配置信息类
 * @Author yuanjx3
 * @Date 2021/1/12 14:08
 * @Version V1.0
 **/
public final class ZookeeperConfiguration {
    /**
     * zookeeper集群节点信息,包含 ip:port。
     * 多个节点间以逗号隔开。
     * 例：
     *      host1:2181,host2:2181
     *  */
    private final String serverLists;
    /** 命名空间 */
    private final String namespace;
    /** 最少休眠时间 */
    private int baseSleepTimeMilliseconds = 10000;
    /** 最多休眠时间 */
    private int maxSleepTimeMilliseconds = 30000;
    /** 最大重试次数 */
    private int maxRetries = 3;
    /** session超时时间 */
    private int sessionTimeoutMilliseconds;
    /** 连接超时时间 */
    private int connectionTimeoutMilliseconds;
    /** Zookeeper digest */
    private String digest;

    public ZookeeperConfiguration(String serverLists, String namespace) {
        this.serverLists = serverLists;
        this.namespace = namespace;
    }

    public String getServerLists() {
        return serverLists;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getBaseSleepTimeMilliseconds() {
        return baseSleepTimeMilliseconds;
    }

    public void setBaseSleepTimeMilliseconds(int baseSleepTimeMilliseconds) {
        this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
    }

    public int getMaxSleepTimeMilliseconds() {
        return maxSleepTimeMilliseconds;
    }

    public void setMaxSleepTimeMilliseconds(int maxSleepTimeMilliseconds) {
        this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getSessionTimeoutMilliseconds() {
        return sessionTimeoutMilliseconds;
    }

    public void setSessionTimeoutMilliseconds(int sessionTimeoutMilliseconds) {
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
    }

    public int getConnectionTimeoutMilliseconds() {
        return connectionTimeoutMilliseconds;
    }

    public void setConnectionTimeoutMilliseconds(int connectionTimeoutMilliseconds) {
        this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
