package org.sunbird.scheduler.util;

import org.springframework.stereotype.Component;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.core.logger.CbExtLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.sunbird.common.util.Constants.*;

/** @author Manzarul All the scheduler job will be handle by this class. */
@Component
public class SchedulerManager<aDate> {

    private static final int PAGE_DATA_TTL = 4;
    static PropertiesCache p = PropertiesCache.getInstance();
    private static final Long SCHEDULER_TIME_GAP = Long.valueOf(p.getProperty(Constants.SCHEDULER_TIME_GAP));
    public static ScheduledExecutorService service = ExecutorManager.getExecutorService();
    private static CbExtLogger logger = new CbExtLogger(SchedulerManager.class.getName());

    /** all scheduler job will be configure here. */
    public static void schedule() {

        logger.info("SchedulerManager:schedule: Started scheduler job for cache refresh.");
        Map<Integer, Integer> dayToDelay = new HashMap<Integer, Integer>();
        Calendar with = Calendar.getInstance();
        Date aDate = new Date();
        with.setTime(aDate);
        dayToDelay.put(Calendar.FRIDAY, 6);
        dayToDelay.put(Calendar.SATURDAY, 5);
        dayToDelay.put(Calendar.SUNDAY, 4);
        dayToDelay.put(Calendar.MONDAY, 3);
        dayToDelay.put(Calendar.TUESDAY, 2);
        dayToDelay.put(Calendar.WEDNESDAY, 1);
        dayToDelay.put(Calendar.THURSDAY, 0);
        int dayOfWeek = with.get(Calendar.DAY_OF_WEEK);
        int hour = with.get(Calendar.HOUR_OF_DAY);
        int delayInDays = dayToDelay.get(dayOfWeek);
        int delayInHours = 0;
        if(delayInDays == 6 && hour<11){
            delayInHours = 11 - hour;
        }else {
            delayInHours = delayInDays * 24 + ((24 - hour) + 11);
        }
        service.scheduleWithFixedDelay(new EmailNotificationService(), 0,
                24, TimeUnit.HOURS);
        logger.info("SchedulerManager:schedule: Started weekly job for incomplete course email to the users.");
    }
}
