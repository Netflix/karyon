package com.netflix.karyon.server.eureka;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.netflix.karyon.spi.HealthCheckHandler;

public class AsyncHealthCheckInvocationStrategyTest {
    public HealthCheckHandler createHealthCheck(final long delay, final TimeUnit units, final int result) {
        return new HealthCheckHandler() {
            @Override
            public int getStatus() {
                try {
                    if (delay > 0) {
                        units.sleep(delay);
                    }
                } catch (InterruptedException e) {
                }
                return result;
            }
        };
    }
    
    public HealthCheckHandler createFailingHealthCheck(final long delay, final TimeUnit units) {
        return new HealthCheckHandler() {
            @Override
            public int getStatus() {
                try {
                    if (delay > 0) {
                        units.sleep(delay);
                    }
                } catch (InterruptedException e) {
                }
                throw new RuntimeException();
            }
        };
    }
    
    @Test(expected=TimeoutException.class)
    public void longerThanTimeoutShouldFail() throws TimeoutException, InterruptedException, ExecutionException {
        HealthCheckHandler handler = createHealthCheck(200, TimeUnit.MILLISECONDS, 200);
        final AsyncHealthCheckInvocationStrategy invoker = new AsyncHealthCheckInvocationStrategy(handler, 100);
        invoker.start();
        
        invoker.invokeCheck();
        
        Assert.fail("Should have failed with timeout exception");
    }
    
    @Test
    public void fastHealthCheck() throws TimeoutException, InterruptedException, ExecutionException {
        HealthCheckHandler handler = createHealthCheck(20, TimeUnit.MILLISECONDS, 200);
        final AsyncHealthCheckInvocationStrategy invoker = new AsyncHealthCheckInvocationStrategy(handler, 100);
        invoker.start();
        
        Assert.assertEquals(200, invoker.invokeCheck());
        Assert.assertEquals(1, invoker.getExecuteCounter());
        Assert.assertEquals(1, invoker.getInvokeCounter());
    }
    
    @Test
    public void fastHealthCheckFailure() throws TimeoutException, InterruptedException, ExecutionException {
        HealthCheckHandler handler = createHealthCheck(20, TimeUnit.MILLISECONDS, 500);
        final AsyncHealthCheckInvocationStrategy invoker = new AsyncHealthCheckInvocationStrategy(handler, 100);
        invoker.start();
        
        Assert.assertEquals(500, invoker.invokeCheck());
        Assert.assertEquals(1, invoker.getExecuteCounter());
        Assert.assertEquals(1, invoker.getInvokeCounter());
    }
    
    static class Result {
        private int code;
        private long duration;

        Result(int code, long duration) {
            this.code = code;
            this.duration = duration;
        }
    }
    
    @Test
    // This is bound to be a flaky test because it depends on sleeps
    public void concurrentHealthCheckReuse() throws TimeoutException, InterruptedException, ExecutionException {
        HealthCheckHandler handler = createHealthCheck(200, TimeUnit.MILLISECONDS, 200);
        final AsyncHealthCheckInvocationStrategy invoker = new AsyncHealthCheckInvocationStrategy(handler, 1000);
        invoker.start();
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // Simulated first calling thread should trigger the health check immediately
        Future<Result> f1 = executor.submit(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                Stopwatch sw = Stopwatch.createStarted();
                int code = invoker.invokeCheck();
                sw.stop();
                return new Result(code, sw.elapsed(TimeUnit.MILLISECONDS));
            }
        });
        
        // Simulated second calling thread delays calling health check for 100 and should attach to the pending
        // health check which should take 200 msec.
        Future<Result> f2 = executor.submit(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                TimeUnit.MILLISECONDS.sleep(100);
                Stopwatch sw = Stopwatch.createStarted();
                int code = invoker.invokeCheck();
                sw.stop();
                return new Result(code, sw.elapsed(TimeUnit.MILLISECONDS));
            }
        });
        
        Result r1 = f1.get();   
        Result r2 = f2.get();   
        
        Assert.assertEquals(200, (int)r1.code);
        Assert.assertEquals(200, (int)r2.code);
        
        // r1's duration should be the expected ~200 msec configured on the HealthCheckhandler
        // r2's duration should be ~100 msec because it started 100 msec after r1 but will use
        // r1's result when it's available. 
        Assert.assertTrue(r1.duration > r2.duration);
        Assert.assertEquals(2, (r1.duration + 50) / 100);  
        Assert.assertEquals(1, (r2.duration + 50) / 100);  
        
        Assert.assertEquals(1, invoker.getExecuteCounter());
        Assert.assertEquals(2, invoker.getInvokeCounter());
    }
    
    // The following method can be used as the base for a stress test
    public void stressTest() {
        HealthCheckHandler handler = createHealthCheck(0, TimeUnit.MILLISECONDS, 200);
        final AsyncHealthCheckInvocationStrategy invoker = new AsyncHealthCheckInvocationStrategy(handler, 1000);
        invoker.start();
        
        for (int i = 0; i < 10000; i++) {
            Stopwatch sw = Stopwatch.createStarted();
            try {
                invoker.invokeCheck();
            } catch (TimeoutException e) {
            } finally {
                sw.stop();
                if (sw.elapsed(TimeUnit.MILLISECONDS) > 10) {
                    System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
                }
                else {
                    System.out.print(".");
                    if (i % 100 == 0) {
                        System.out.println("");
                    }
                }
            }
        }
    }
}
