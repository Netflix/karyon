package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

/**
 * @author Nitesh Kant
 */
public class HealthCheckGuy implements HealthCheckHandler {

    public HealthCheckGuy() {
        RegistrationSequence.addClass(this.getClass());
    }

    @Override
    public int getStatus() {
        return 200;
    }
}
