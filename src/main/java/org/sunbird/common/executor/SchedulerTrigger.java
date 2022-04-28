package org.sunbird.common.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.sunbird.common.helper.cassandra.CassandraConnectionManager;
import org.sunbird.common.helper.cassandra.CassandraConnectionMngrFactory;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.KeyManager;
import org.sunbird.scheduler.util.SchedulerManager;

@Component
public class SchedulerTrigger implements ApplicationRunner {

    public static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTrigger.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        CassandraConnectionManager manager = CassandraConnectionMngrFactory.getInstance();
        manager.createConnection();
        SchedulerManager.schedule();
    }

}