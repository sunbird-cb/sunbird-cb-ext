package org.sunbird.common.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.sunbird.common.helper.cassandra.CassandraConnectionMngrFactory;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.scheduler.util.SchedulerManager;

import static org.sunbird.common.util.Constants.SEND_NOTIFICATION_PROPERTIES;

@Component
public class SchedulerTrigger implements ApplicationRunner {

    public static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTrigger.class);
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(SEND_NOTIFICATION_PROPERTIES))) {
            CassandraConnectionMngrFactory.getInstance().createConnection();
            SchedulerManager.schedule();
        }
    }

}