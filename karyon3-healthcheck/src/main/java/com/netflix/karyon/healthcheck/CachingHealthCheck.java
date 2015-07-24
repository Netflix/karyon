package com.netflix.karyon.healthcheck;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
    public HealthStatus check() {
        long lastExpireTime = this.expireTime.get();
        long currentTime  = System.nanoTime();
        
        if (currentTime > lastExpireTime + interval) {
            long expireTime = currentTime + interval;
            if (this.expireTime.compareAndSet(lastExpireTime, expireTime)) {
                if (busy.compareAndSet(false, true)) {
                    try {
                        status = delegate.check();
                    }
                    catch (Exception e) {
                        status = HealthStatuses.unhealthy(e);
                    }
                    finally {
                        busy.set(false);
                    }
                }
            }
        }
        
        return status;
    }
}
