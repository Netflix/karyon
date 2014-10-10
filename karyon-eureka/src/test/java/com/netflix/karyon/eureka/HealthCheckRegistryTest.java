package com.netflix.karyon.eureka;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.MockRemoteEurekaServer;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorMode;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheckInvoker;
import com.netflix.karyon.health.HealthCheckRegistry;

public class HealthCheckRegistryTest {
    public static final String REMOTE_REGION = "myregion";
    public static final String REMOTE_ZONE = "myzone";
    
    @Singleton
    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            ConfigurationManager.getConfigInstance().setProperty("eureka.shouldFetchRegistry", "true");
            ConfigurationManager.getConfigInstance().setProperty("eureka.responseCacheAutoExpirationInSeconds", "10");
            ConfigurationManager.getConfigInstance().setProperty("eureka.client.refresh.interval", "60");
            ConfigurationManager.getConfigInstance().setProperty("eureka.registration.enabled", "false");
            ConfigurationManager.getConfigInstance().setProperty("eureka.validateInstanceId", "false");
            ConfigurationManager.getConfigInstance().setProperty("eureka.fetchRemoteRegionsRegistry", REMOTE_REGION);
            ConfigurationManager.getConfigInstance().setProperty("eureka.myregion.availabilityZones", REMOTE_ZONE);
            ConfigurationManager.getConfigInstance().setProperty("eureka.serviceUrl.default",
                                                                 "http://localhost:" + 7777 +
                                                                 MockRemoteEurekaServer.EUREKA_API_BASE_PATH);

            DataCenterInfo myDCI = new DataCenterInfo() {
                public DataCenterInfo.Name getName() { return DataCenterInfo.Name.MyOwn; }
            };

            bind(HealthCheckRegistry.class).to(DefaultHealthCheckRegistry.class);
            bind(com.netflix.appinfo.HealthCheckHandler.class).to(EurekaHealthCheckHandler.class);
            bind(InstanceInfo.class).toInstance(
                    InstanceInfo.Builder.newBuilder()
                        .setAppName("test")
                        .setStatus(InstanceStatus.STARTING)
                        .setDataCenterInfo(myDCI).build());
        }
    }
    
    @Test
    public void defaultShouldBeStarting() {
        Injector injector = LifecycleInjector.builder()
                    .withModuleClasses(TestModule.class)
                    .build()
                    .createInjector();
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        com.netflix.appinfo.HealthCheckHandler handler = injector.getInstance(com.netflix.appinfo.HealthCheckHandler.class);
        
        Assert.assertEquals(1, registry.getHealthChecks().size());
        
        Assert.assertEquals(InstanceStatus.STARTING, handler.getStatus(null));
        
        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
        Assert.assertEquals(InstanceStatus.UP, handler.getStatus(null));
    }
    
    @Test
    public void defaultShouldBeStartingWithDefaultHealthCheck() {
        final AtomicInteger status = new AtomicInteger(204);
        
        Injector injector = LifecycleInjector.builder()
                .withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS)
                .withModuleClasses(TestModule.class)
                .withAdditionalModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthCheckHandler.class).toInstance(new HealthCheckHandler() {
                            @Override
                            public int getStatus() {
                                return status.get();
                            }
                        });
                    }
                })
                .build()
                .createInjector();
        HealthCheckHandler hcHandler = injector.getInstance(HealthCheckHandler.class);
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        HealthCheckInvoker invoker   = injector.getInstance(HealthCheckInvoker.class);
        com.netflix.appinfo.HealthCheckHandler handler = injector.getInstance(com.netflix.appinfo.HealthCheckHandler.class);

        Assert.assertNotNull(hcHandler);
        
        Assert.assertEquals(2, registry.getHealthChecks().size());
        
        Assert.assertEquals(InstanceStatus.STARTING, handler.getStatus(null));
        
        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
        status.set(200);
        
        Assert.assertEquals(InstanceStatus.UP, handler.getStatus(null));
        
        status.set(500);
        
        Assert.assertEquals(InstanceStatus.DOWN, handler.getStatus(null));
        
        System.out.println(registry.getHealthChecks());
        System.out.println(invoker.invoke(registry.getHealthChecks()));
    }
    
    @Test
    public void reportExceptionForFailingHealthCheck() {
        Injector injector = LifecycleInjector.builder()
                .withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS)
                .withModuleClasses(TestModule.class)
                .withAdditionalModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthCheckHandler.class).toInstance(new HealthCheckHandler() {
                            @Override
                            public int getStatus() {
                                throw new RuntimeException("Failed");
                            }
                        });
                    }
                })
                .build()
                .createInjector();
        HealthCheckHandler hcHandler = injector.getInstance(HealthCheckHandler.class);
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        HealthCheckInvoker invoker   = injector.getInstance(HealthCheckInvoker.class);
        com.netflix.appinfo.HealthCheckHandler handler = injector.getInstance(com.netflix.appinfo.HealthCheckHandler.class);
        
        Assert.assertNotNull(hcHandler);
        
        Assert.assertEquals(2, registry.getHealthChecks().size());
        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
        
        Assert.assertEquals(InstanceStatus.DOWN, handler.getStatus(null));
        
        System.out.println(registry.getHealthChecks());
        System.out.println(invoker.invoke(registry.getHealthChecks()));
    }
}
