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

    /**
     * all scheduler job will be configured here.
     */
    public static void schedule() {
        logger.info("SchedulerManager:schedule: Started scheduler job for email notifications.");
        // Today's Date Calculation
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        logger.debug("Today's Calendar Date : " + todayCal.getTime());
        // Next Scheduler Run Date & Time Calculation
        Calendar incompleteCoursesTargetCal = calculateTargetCalender(todayCal, Integer.parseInt(p.getProperty(Constants.INCOMPLETE_COURSES_SCHEDULER_RUN_DAY)), Integer.parseInt(p.getProperty(Constants.INCOMPLETE_COURSES_SCHEDULER_RUN_TIME)));
        Calendar newCoursesTargetCal = calculateTargetCalender(todayCal, Integer.parseInt(p.getProperty(Constants.NEW_COURSES_SCHEDULER_RUN_DAY)), Integer.parseInt(p.getProperty(Constants.NEW_COURSES_SCHEDULER_RUN_TIME)));
        service.scheduleWithFixedDelay(new IncompleteCoursesEmailNotificationService(), ((incompleteCoursesTargetCal.getTimeInMillis() - todayCal.getTimeInMillis()) / 1000) / 60,
                Long.valueOf(p.getProperty(Constants.INCOMPLETE_COURSES_SCHEDULER_TIME_GAP)), TimeUnit.MINUTES);
//        service.scheduleWithFixedDelay(new NewCoursesEmailNotificationService(), ((newCoursesTargetCal.getTimeInMillis() - todayCal.getTimeInMillis()) / 1000) / 60,
//                Long.valueOf(p.getProperty(Constants.NEW_COURSES_SCHEDULER_TIME_GAP)), TimeUnit.MINUTES);
        service.scheduleWithFixedDelay(new NewCoursesEmailNotificationService(), 0,
               7, TimeUnit.MINUTES);
        logger.info("SchedulerManager:schedule: Started job for sending emails to the users.");
    }

    private static Calendar calculateTargetCalender(Calendar todayCal, int runDay, int runTime) {
        Calendar targetCal = Calendar.getInstance();

        // Setting 6 - 6th Day of the Week : Friday
        targetCal.set(Calendar.DAY_OF_WEEK, runDay);

        // can be 0-23 - Eg: For Evening 5PM it will be 17
        targetCal.set(Calendar.HOUR_OF_DAY, runTime);

        // Setting 0 - For the Minute Value of 05:00
        targetCal.set(Calendar.MINUTE, 0);

        // Setting 0 - For the Second Value of 05:00:00
        targetCal.set(Calendar.SECOND, 0);
        logger.debug("Target Calendar Date : " + targetCal.getTime());

        if (todayCal.after(targetCal)) {
            targetCal.set(Calendar.WEEK_OF_YEAR, targetCal.getWeeksInWeekYear() + 1);
        }
        return targetCal;
    }
}