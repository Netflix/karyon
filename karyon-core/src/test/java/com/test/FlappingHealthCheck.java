package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nitesh Kant
 */
public class FlappingHealthCheck implements HealthCheckHandler {

    private AtomicBoolean returnSuccess = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        RegistrationSequence.addClass(this.getClass());
    }

    @Override
    public int getStatus() {
        if (returnSuccess.compareAndSet(true, false)) {
            return 200;
        } else {
            return 500;
        }
    }
}
