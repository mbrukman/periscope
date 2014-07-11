package com.sequenceiq.periscope.monitor.event.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.sequenceiq.periscope.monitor.event.QueueInfoUpdateEvent;

@Component
public class QueueInfoUpdateEventHandler implements ApplicationListener<QueueInfoUpdateEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueInfoUpdateEventHandler.class);

    @Override
    public void onApplicationEvent(QueueInfoUpdateEvent event) {
        LOGGER.info("Queue metrics updated for cluster.. {}", event.getClusterId());
    }
}