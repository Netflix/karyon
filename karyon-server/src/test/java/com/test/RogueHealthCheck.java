package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class RogueHealthCheck implements HealthCheckHandler {

    @PostConstruct
    public void init() {
        RegistrationSequence.addClass(this.getClass());
    }

    @Override
    public int checkHealth() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 200;
    }
}
