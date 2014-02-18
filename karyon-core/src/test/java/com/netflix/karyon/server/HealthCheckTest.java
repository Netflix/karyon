package com.netflix.karyon.server;

import com.netflix.karyon.server.bootstrap.AlwaysHealthyHealthCheck;
import com.netflix.karyon.server.bootstrap.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.bootstrap.HealthCheckInvocationStrategy;
import com.netflix.karyon.server.bootstrap.SyncHealthCheckInvocationStrategy;
import com.test.FlappingHealthCheck;
import com.test.RogueHealthCheck;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Nitesh Kant
 */
public class HealthCheckTest {

    @Nullable private KaryonServer server;

    @After
    public void tearDown() throws Exception {
        if (null != server) {
            server.stop();
        }
    }

    @Test(expected = TimeoutException.class)
    public void testAsyncStrategyTimeout() throws Exception {
        HealthCheckInvocationStrategy strategy = new AsyncHealthCheckInvocationStrategy(new RogueHealthCheck(), 10);
        strategy.invokeCheck();
    }

    @Test
    public void testFlappingHealthCheck() throws Exception {
        HealthCheckInvocationStrategy strategy = new SyncHealthCheckInvocationStrategy(new FlappingHealthCheck());

        assertEquals("First health check did not pass.", 200, strategy.invokeCheck());
        assertNotSame("Second health check did not fail.", 200, strategy.invokeCheck());
    }

    @Test
    public void testHealthCheckSuccess() throws Exception {
        HealthCheckInvocationStrategy strategy = new SyncHealthCheckInvocationStrategy(AlwaysHealthyHealthCheck.INSTANCE);
        assertEquals("Health check failed.", 200, strategy.invokeCheck());
    }
}
