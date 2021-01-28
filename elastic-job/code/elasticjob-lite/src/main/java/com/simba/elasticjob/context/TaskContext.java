package com.simba.elasticjob.context;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/21 19:46
 * @Version V1.0
 **/
public class TaskContext {
    private static final String DELIMITER = "@-@";

    private static final String UNASSIGNED_SLAVE_ID = "unassigned-slave";

    private String id;

    private final MetaInfo metaInfo;

    private final ExecutionType type;

    private String slaveId;

    private boolean idle;

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public TaskContext(final String jobName, final List<Integer> shardingItem, final ExecutionType type) {
        this(jobName, shardingItem, type, UNASSIGNED_SLAVE_ID);
    }

    public TaskContext(final String jobName, final List<Integer> shardingItem, final ExecutionType type, final String slaveId) {
        metaInfo = new MetaInfo(jobName, shardingItem);
        this.type = type;
        this.slaveId = slaveId;
        id = String.join(DELIMITER, metaInfo.toString(), type.toString(), slaveId, UUID.randomUUID().toString());
    }

    private TaskContext(final String id, final MetaInfo metaInfo, final ExecutionType type, final String slaveId) {
        this.id = id;
        this.metaInfo = metaInfo;
        this.type = type;
        this.slaveId = slaveId;
    }

    /**
     * Get task context via task ID.
     *
     * @param id task ID
     * @return task context
     */
    public static TaskContext from(final String id) {
        String[] result = id.split(DELIMITER);
        Preconditions.checkState(5 == result.length);
        return new TaskContext(id, MetaInfo.from(result[0] + DELIMITER + result[1]), ExecutionType.valueOf(result[2]), result[3]);
    }

    /**
     * Get unassigned task ID before job execute.
     *
     * @param id task ID
     * @return unassigned task ID before job execute
     */
    public static String getIdForUnassignedSlave(final String id) {
        return id.replaceAll(TaskContext.from(id).getSlaveId(), UNASSIGNED_SLAVE_ID);
    }

    /**
     * Set job server ID.
     *
     * @param slaveId job server ID
     */
    public void setSlaveId(final String slaveId) {
        id = id.replaceAll(this.slaveId, slaveId);
        this.slaveId = slaveId;
    }

    /**
     * Get task name.
     *
     * @return task name
     */
    public String getTaskName() {
        return String.join(DELIMITER, metaInfo.toString(), type.toString(), slaveId);
    }

    /**
     * Get executor ID.
     *
     * @param appName application name
     * @return executor ID
     */
    public String getExecutorId(final String appName) {
        return String.join(DELIMITER, appName, slaveId);
    }

    /**
     * Task meta data.
     */
    public static class MetaInfo {

        private final String jobName;

        private final List<Integer> shardingItems;

        public MetaInfo(String jobName, List<Integer> shardingItems) {
            this.jobName = jobName;
            this.shardingItems = shardingItems;
        }

        public String getJobName() {
            return jobName;
        }

        public List<Integer> getShardingItems() {
            return shardingItems;
        }

        /**
         * Get task meta data info via string.
         *
         * @param value task meta data info string
         * @return task meta data info
         */
        public static MetaInfo from(final String value) {
            String[] result = value.split(DELIMITER);
            Preconditions.checkState(1 == result.length || 2 == result.length || 5 == result.length);
            return new MetaInfo(result[0], 1 == result.length || "".equals(result[1])
                    ? Collections.emptyList() : Splitter.on(",").splitToList(result[1]).stream().map(Integer::parseInt).collect(Collectors.toList()));
        }

        @Override
        public final String toString() {
            return String.join(DELIMITER, jobName, shardingItems.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
    }

    public static String getDELIMITER() {
        return DELIMITER;
    }

    public static String getUnassignedSlaveId() {
        return UNASSIGNED_SLAVE_ID;
    }

    public String getId() {
        return id;
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public ExecutionType getType() {
        return type;
    }

    public String getSlaveId() {
        return slaveId;
    }

    public boolean isIdle() {
        return idle;
    }
}
