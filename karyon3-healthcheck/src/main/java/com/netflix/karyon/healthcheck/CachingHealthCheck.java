package com.netflix.karyon.healthcheck;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Health check implementation that caches the response
 *
 * @author elandau
 *
 */
public class CachingHealthCheck implements HealthCheck {
    private final AtomicLong      expireTime = new AtomicLong(0);
    private final long            interval;
    private final HealthCheck     delegate;
    private final AtomicBoolean   busy = new AtomicBoolean();
    private volatile HealthStatus status;
    
    public CachingHealthCheck(HealthCheck delegate, long interval, TimeUnit units) {
        this.delegate = delegate;
        this.interval = TimeUnit.NANOSECONDS.convert(interval, units);
    }
    
    @Override
    public CompletableFuture<HealthStatus> check() {
        long lastExpireTime = this.expireTime.get();
        long currentTime  = System.nanoTime();
        
        if (currentTime > lastExpireTime + interval) {
            long expireTime = currentTime + interval;
            if (this.expireTime.compareAndSet(lastExpireTime, expireTime)) {
                if (busy.compareAndSet(false, true)) {
                    return delegate.check().whenComplete(new BiConsumer<HealthStatus, Throwable>() {
                        @Override
                        public void accept(HealthStatus t, Throwable u) {
                            try {
                                if (t != null) {
                                    status = t;
                                }
                                else if (u != null) {
                                    status = HealthStatuses.unhealthy(u);
                                }
                                else {
                                    status = HealthStatuses.unhealthy(new Exception("Unknown"));
                                }
                            }
                            finally {
                                busy.set(false);
                            }
                        }
                    });
                }
            }
        }
        
        return CompletableFuture.completedFuture(status);
    }
}
