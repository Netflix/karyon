package com.netflix.karyon.example.jetty;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LongDelayService {
    @Inject
    public LongDelayService() {
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
