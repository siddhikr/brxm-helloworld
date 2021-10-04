package com.example.learning.brxm.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

public class MyJCREventListener implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(MyJCREventListener.class);

    @Override
    public void onEvent(EventIterator events) {
        log.info("Inside onEvent");
        while (events.hasNext()) {
            Event event = events.nextEvent();
            log.info("Inside event loop "+ event.toString());
        }
    }
}