package org.ibp.controller;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ContextRefreshedListener.class);

    @Autowired
    private Scheduler scheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent cse) {
        try {
            // Scheduler must start once the application has started because we want to run liquibase before any trigger is fired
            scheduler.start();
            LOG.info("Quartz scheduler started.");
        } catch (SchedulerException e) {
            LOG.error("An error occurred trying to start quartz scheduler: ", e);
        }
    }
}
