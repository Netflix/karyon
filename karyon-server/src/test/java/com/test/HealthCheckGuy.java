package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

/**
 * @author Nitesh Kant (nkant@netflix.com)
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
