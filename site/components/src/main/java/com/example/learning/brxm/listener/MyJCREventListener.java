package com.example.learning.brxm.listener;

import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyJCREventListener {

    private static final Logger log = LoggerFactory.getLogger(MyJCREventListener.class);

    @Subscribe
    public void handleEvent(HippoEvent event) {
        log.info("Inside handleEvent event is:" + event.action());
    }

}