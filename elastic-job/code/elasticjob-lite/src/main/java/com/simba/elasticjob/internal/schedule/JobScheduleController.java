package com.simba.elasticjob.internal.schedule;

import com.simba.elasticjob.exception.JobSystemException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 任务调度控制器 (这里通过调用quartz的API进行任务调度管理的)
 * @Author yuanjx3
 * @Date 2021/1/15 16:17
 * @Version V1.0
 **/
public class JobScheduleController {
    Logger log = LoggerFactory.getLogger(getClass());
    private final Scheduler scheduler;
    private final JobDetail jobDetail;
    private final String triggerIdentity;

    public JobScheduleController(Scheduler scheduler, JobDetail jobDetail, String triggerIdentity) {
        log.debug("创建任务调度控制器，scheduler: " + scheduler + ", jobDetail: " + ", triggerIdentity: " + triggerIdentity);
        this.scheduler = scheduler;
        this.jobDetail = jobDetail;
        this.triggerIdentity = triggerIdentity;
    }

    /** 功能描述: 根据传入的cron表达式， 调度执行job
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:23
    */
    public void shedulerJob(String cron){
        try {
            if (!scheduler.checkExists(jobDetail.getKey())){
                log.debug("根据传入的cron表达式， 调度执行job，cron: " + cron + ", jobDetail " + jobDetail);
                scheduler.scheduleJob(jobDetail, createCronTrigger(cron));
            }
            scheduler.start();
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }

    /** 功能描述: 根据传入的cron表达式， 重新调度执行job
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:23
     */
    public synchronized void reshedulerJob(String cron){
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(TriggerKey.triggerKey(triggerIdentity));
            //如果任务调度器未关闭，触发器不为空，且触发器的cron与新传入的值不同，则进行重新调度
            if (!scheduler.isShutdown() && null!=trigger && !cron.equals(trigger.getCronExpression())){
                log.debug("根据新传入的cron表达式， 重新调度调度job，new cron: " + cron + ", jobDetail " + jobDetail);
                scheduler.rescheduleJob(TriggerKey.triggerKey(triggerIdentity), createCronTrigger(cron));
            }
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }

    /** 功能描述: 重新调度执行一个一次性的job
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:23
     */
    public synchronized void reshedulerJob(){
        try {
            SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(TriggerKey.triggerKey(triggerIdentity));
            //如果任务调度器未关闭，触发器不为空，且触发器的cron与新传入的值不同，则进行重新调度
            if (!scheduler.isShutdown() && null!=trigger){
                log.debug("根据新传入的cron表达式， 重新调度调度One Off job，jobDetail " + jobDetail);
                scheduler.rescheduleJob(TriggerKey.triggerKey(triggerIdentity), createOneOffTrigger());
            }
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }
    
    /** 功能描述: 根据cron生成一个计划任务的触发器
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:35
    */
    private Trigger createCronTrigger(String cron) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerIdentity)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                .build();
    }

    /** 功能描述: 创建只调度一次的触发器
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:39
    */
    private Trigger createOneOffTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerIdentity)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();
    }
    
    /** 功能描述:   判断任务是否暂停调度
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:43
    */
    public synchronized boolean isPaused(){
        try {
            return !scheduler.isShutdown() && Trigger.TriggerState.PAUSED == scheduler.getTriggerState(new TriggerKey(triggerIdentity));
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }

    /** 功能描述:
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:43
     */
    public synchronized void pauseJob(){
        try {
            if (!scheduler.isShutdown()){
                log.debug("pause All job");
                scheduler.pauseAll();
            }
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }

    /** 功能描述:   唤醒任务调度
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:43
     */
    public synchronized void resumeJob(){
        try {
            if (!scheduler.isShutdown()){
                log.debug("resume All job");
                scheduler.resumeAll();
            }
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }

    /** 功能描述:   触法任务执行
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:43
     */
    public synchronized void triggerJob(){
        try {
            if (scheduler.isShutdown()){
                return;
            }
            if (!scheduler.checkExists(jobDetail.getKey())){
                log.debug("trigger One Off Job");
                scheduler.scheduleJob(jobDetail,createOneOffTrigger());
            }else{
                log.debug("trigger cron Job");
                scheduler.triggerJob(jobDetail.getKey());
            }
            if (!scheduler.isStarted()){
                scheduler.start();
            }
        } catch (SchedulerException e) {
            throw new JobSystemException(e);
        }
    }
    
    /** 功能描述:  关闭任务调度器
    * @Author: yuanjx3
    * @Date: 2021/1/15 16:51
    */
    public synchronized void shutdown(){
        shutdown(false);
    }

    /** 功能描述:  优雅的关闭任务调度器
     * @param: isCleanShutdown 是否等待job执行完毕后再停机
     *          （true：等待job执行完毕后关闭调度器；false: 立即关闭）
     * @Author: yuanjx3
     * @Date: 2021/1/15 16:51
     */
    public synchronized void shutdown(boolean isCleanShutdown){
        try {
            if (!scheduler.isShutdown()){
                log.debug("优雅的关闭任务调度器");
                scheduler.shutdown(isCleanShutdown);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
