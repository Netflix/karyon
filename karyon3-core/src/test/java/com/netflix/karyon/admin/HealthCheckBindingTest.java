package com.netflix.karyon.admin;

import javax.inject.Named;

import org.junit.Test;

import com.google.inject.Provides;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthCheckModule;
import com.netflix.karyon.healthcheck.HealthCheckRegistry;
import com.netflix.karyon.healthcheck.HealthStatuses;

public class HealthCheckBindingTest {
    @Test
    public void test() {
        LifecycleInjector injector = Governator.createInjector(
            new HealthCheckModule(),
            new CoreAdminModule(),
            new DefaultModule() {
                @Provides
                @Named("hc1")
                public HealthCheck getHealthCheck1() {
                    return () -> HealthStatuses.healthy(); 
                }
                
                @Provides
                @Named("hc2")
                public HealthCheck getHealthCheck2() {
                    return () -> HealthStatuses.unhealthy(new Exception("foo")); 
                }
            });
        
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        HealthInfoAdminResource res = injector.getInstance(HealthInfoAdminResource.class);
        System.out.println(res.get());
    }
}
