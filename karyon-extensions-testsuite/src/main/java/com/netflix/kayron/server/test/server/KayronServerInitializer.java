package com.netflix.kayron.server.test.server;

import com.netflix.kayron.server.test.RunInKaryon;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.Before;

/**
 * A test initializer that sets up the Kayron environment in the server.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronServerInitializer {

    /**
     * Handles the before test event.
     *
     * @param event the before test event
     */
    public void beforeTest(@Observes Before event) {

        RunInKaryon annotation = event.getTestClass().getAnnotation(RunInKaryon.class);

        if (annotation != null) {

            if (!annotation.applicationId().isEmpty()) {
                System.setProperty("archaius.deployment.applicationId", annotation.applicationId());
            }

            if (!annotation.environment().isEmpty()) {
                System.setProperty("archaius.deployment.environment", annotation.environment());
            }
        }
    }
}
