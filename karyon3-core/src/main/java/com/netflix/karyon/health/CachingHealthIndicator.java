package com.netflix.karyon.health;

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
public class CachingHealthIndicator implements HealthIndicator {
    private final AtomicLong      expireTime = new AtomicLong(0);
    private final long            interval;
    private final HealthIndicator delegate;
    private final AtomicBoolean   busy = new AtomicBoolean();
    private volatile HealthIndicatorStatus status;
    
    public CachingHealthIndicator(HealthIndicator delegate, long interval, TimeUnit units) {
        this.delegate = delegate;
        this.interval = TimeUnit.NANOSECONDS.convert(interval, units);
    }
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        long lastExpireTime = this.expireTime.get();
        long currentTime  = System.nanoTime();
        
        if (currentTime > lastExpireTime + interval) {
            long expireTime = currentTime + interval;
            if (this.expireTime.compareAndSet(lastExpireTime, expireTime)) {
                if (busy.compareAndSet(false, true)) {
                    return delegate.check().whenComplete(new BiConsumer<HealthIndicatorStatus, Throwable>() {
                        @Override
                        public void accept(HealthIndicatorStatus t, Throwable u) {
                            try {
                                if (t != null) {
                                    status = t;
                                }
                                else if (u != null) {
                                    status = HealthIndicatorStatuses.unhealthy(getName(), u);
                                }
                                else {
                                    status = HealthIndicatorStatuses.unhealthy(getName(), new Exception("Unknown"));
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

    @Override
    public String getName() {
        return delegate.getName();
    }
}
