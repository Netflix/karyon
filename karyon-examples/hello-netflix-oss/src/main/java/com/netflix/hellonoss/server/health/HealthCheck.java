package com.netflix.hellonoss.server.health;

import com.netflix.karyon.spi.HealthCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class HealthCheck implements HealthCheckHandler {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    @PostConstruct
    public void init() {
        logger.info("Health check initialized.");
    }

    @Override
    public int getStatus() {
        // TODO: Health check logic.
        logger.info("Health check invoked.");
        return 200;
    }
}
