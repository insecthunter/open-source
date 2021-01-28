package com.simba.elasticjob.internal.schedule;

import com.simba.elasticjob.internal.election.LeaderService;
import com.simba.elasticjob.internal.instance.InstanceService;
import com.simba.elasticjob.register.base.CoordinatorRegistryCenter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/22 9:30
 * @Version V1.0
 **/
public class JobShutdownHookPlugin implements SchedulerPlugin {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String jobName;
    private boolean cleanShutdown = true;

    @Override
    public void initialize(final String name, final Scheduler scheduler, final ClassLoadHelper classLoadHelper) throws SchedulerException {
        jobName = scheduler.getSchedulerName();
        registerShutdownHook();
    }

    /**
     * Called when the associated <code>Scheduler</code> is started, in order
     * to let the plug-in know it can now make calls into the scheduler if it
     * needs to.
     */
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        CoordinatorRegistryCenter regCenter = JobRegistry.getInstance().getRegCenter(jobName);
        if (null == regCenter) {
            return;
        }
        LeaderService leaderService = new LeaderService(regCenter, jobName);
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        new InstanceService(regCenter, jobName).removeInstance();
    }

    private void registerShutdownHook() {
        log.info("Registering Quartz shutdown hook. {}", jobName);
        System.out.println("Registering Quartz shutdown hook. "+ jobName);
        Thread t = new Thread("Quartz Shutdown-Hook " + jobName) {
            @Override
            public void run() {
                log.info("Shutting down Quartz... {}", jobName);
                JobScheduleController scheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
                if (null != scheduleController) {
                    scheduleController.shutdown(isCleanShutdown());
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(t);
    }

    public void setCleanShutdown(boolean cleanShutdown) {
        this.cleanShutdown = cleanShutdown;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isCleanShutdown() {
        return cleanShutdown;
    }

}
