package org.sunbird.scheduler.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class SchedulerManager {

    static PropertiesCache p = PropertiesCache.getInstance();
    public static ScheduledExecutorService service = ExecutorManager.getExecutorService();
    private static CbExtLogger logger = new CbExtLogger(SchedulerManager.class.getName());

    /** all scheduler job will be configured here. */
    public static void schedule() {
        logger.info("SchedulerManager:schedule: Started scheduler job for email notifications.");
        // Today's Date Calculation
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        logger.debug("Today's Calendar Date : " + todayCal.getTime().toString());

        // Next Scheduler Run Date & Time Calculation
        Calendar targetCal = Calendar.getInstance();

        // Setting 6 - 6th Day of the Week : Friday
        targetCal.set(Calendar.DAY_OF_WEEK, Integer.parseInt(p.getProperty(Constants.SCHEDULER_RUN_DAY)));

        // can be 0-23 - Eg: For Evening 5PM it will be 17
        targetCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(p.getProperty(Constants.SCHEDULER_RUN_TIME)));

        // Setting 0 - For the Minute Value of 05:00
        targetCal.set(Calendar.MINUTE, 0);

        // Setting 0 - For the Second Value of 05:00:00
        targetCal.set(Calendar.SECOND, 0);
        logger.debug("Target Calendar Date : " + targetCal.getTime().toString());

        if (todayCal.after(targetCal)) {
            targetCal.set(Calendar.WEEK_OF_YEAR, targetCal.getWeeksInWeekYear() + 1);
        }

        service.scheduleWithFixedDelay(new EmailNotificationService(), ((targetCal.getTimeInMillis() - todayCal.getTimeInMillis()) / 1000) / 60,
                Long.valueOf(p.getProperty(Constants.SCHEDULER_TIME_GAP)), TimeUnit.MINUTES);
        logger.info("SchedulerManager:schedule: Started weekly job for incomplete course email to the users.");
    }
}