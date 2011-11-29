package com.nhn.socialanalytics.twitter.collect;

import java.util.Date;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.nhn.socialanalytics.common.Config;
import com.nhn.socialanalytics.common.JobLogger;

public class TwitterDataCollectorJobTrigger {
	// logger
	private static JobLogger logger = JobLogger.getLogger(TwitterDataCollectorJobTrigger.class, "twitter-collect.log");
	
	public void run() throws Exception {

		System.out.println("------- Initializing -------------------");
		logger.info("------- Initializing -------------------");
		
		// first we must get a reference to a scheduler
		SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        System.out.println("------- Initialization Complete --------");
        System.out.println("------- Scheduling Jobs ----------------");
        
        logger.info("------- Initialization Complete --------");
        logger.info("------- Scheduling Jobs ----------------");
		
		// get a "nice round" time a few seconds in the future...
        //long ts = TriggerUtils.getNextGivenSecondDate(null, 15).getTime();

		// jobs can be scheduled before sched.start() has been called
		// job will run every 60 minutes
        
        String schedule = Config.getProperty("TWITTER_COLLECT_SCHEDULE");
		JobDetail job = new JobDetail("TwitterDataCollectorJob", "twitter", TwitterDataCollectorJob.class);
		
		//Map map = job.getJobDataMap();
		//map.put("key", "value");
		
        CronTrigger trigger = new CronTrigger("TwitterCollectorJobTrigger", "twitter", "TwitterDataCollectorJob",
                "twitter", schedule);	//second, minute, hour, dayOfMonth, month, year
        sched.addJob(job, true);
        Date ft1 = sched.scheduleJob(trigger);
        
        System.out.println(job.getFullName() + " has been scheduled to run at: " + ft1
                + " and repeat based on expression: "
                + trigger.getCronExpression());
        
        logger.info(job.getFullName() + " has been scheduled to run at: " + ft1
                + " and repeat based on expression: "
                + trigger.getCronExpression());

         
        System.out.println("------- Starting Scheduler ----------------");        
        logger.info("------- Starting Scheduler ----------------");
        
        // All of the jobs have been added to the scheduler, but none of the
        // jobs
        // will run until the scheduler has been started
        sched.start();

        System.out.println("------- Started Scheduler -----------------");	
        logger.info("------- Started Scheduler -----------------");	
	}

	public static void main(String[] args) throws Exception {
		TwitterDataCollectorJobTrigger trigger = new TwitterDataCollectorJobTrigger();
		trigger.run();
	}	
}
