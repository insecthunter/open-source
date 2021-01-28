package com.simba.elasticjob.concurrent;

import com.simba.elasticjob.utils.Preconditions;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/17 14:56
 * @Version V1.0
 **/
public abstract class AbstractScheduledService implements Service {
//    private final AbstractService delegate = new AbstractScheduledService.ServiceDelegate();

    protected AbstractScheduledService(){}

    protected abstract void runOneIteration() throws Exception;
    protected void shutdown() throws Exception{}
    protected abstract AbstractScheduledService.Scheduler scheduler();

    public abstract static class Scheduler{
        public static Scheduler newFixedDelaySchedule(Duration initialDelay, Duration delay){
            return newFixedDelaySchedule(Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
        }

        public static AbstractScheduledService.Scheduler newFixedDelaySchedule(long initialDelay, long delay, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            Preconditions.checkArgument(delay>0L,"参数delay必须大于0，入参为 %s",delay);
            return new AbstractScheduledService.Scheduler(){
                public Future<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable task){
                    return executor.scheduleWithFixedDelay(task,initialDelay,delay,unit);
                }
            };
        }
    }
}
