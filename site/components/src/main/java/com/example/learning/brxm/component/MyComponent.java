package com.example.learning.brxm.component;

import com.example.learning.brxm.listener.MyJCREventListener;
import org.onehippo.cms7.services.eventbus.HippoEventListenerRegistry;

public class MyComponent {

    private MyJCREventListener listener;

    public void init() {
        listener = new MyJCREventListener();
        HippoEventListenerRegistry.get().register(listener);
    }

    public void destroy() {
        HippoEventListenerRegistry.get().unregister(listener);
    }

}