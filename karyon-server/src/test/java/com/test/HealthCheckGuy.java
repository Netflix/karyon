package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class HealthCheckGuy implements HealthCheckHandler {

    @PostConstruct
    public void init() {
        RegistrationSequence.addClass(this.getClass());
    }

    @Override
    public int checkHealth() {
        return 200;
    }
}
