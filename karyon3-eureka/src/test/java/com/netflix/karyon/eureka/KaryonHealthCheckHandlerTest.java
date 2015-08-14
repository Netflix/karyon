package com.netflix.karyon.eureka;

import javax.inject.Inject;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.Governator;
import com.netflix.karyon.ApplicationLifecycle;
import com.netflix.karyon.HealthCheck;
import com.netflix.karyon.HealthState;
import com.netflix.karyon.LifecycleState;
import com.netflix.karyon.ManualApplicationLifecycleState;

public class KaryonHealthCheckHandlerTest {
	@Singleton
	public static class Tracker {
		InstanceStatus initialStatus;
		HealthCheckHandler handler;
		
		@Inject
		public void setHealthCheckHandler(HealthCheckHandler handler) {
			this.handler = handler;
			this.initialStatus = handler.getStatus(InstanceStatus.STARTING);
		}
	}
	
	@Singleton
	public static class FailedSingleton {
		@Inject
		FailedSingleton() {
			throw new RuntimeException("Forced failure");
		}
	}
	
	@Test
	public void testSuccessful() {
		final Tracker tracker = new Tracker();
		Injector injector = Governator.createInjector(new ArchaiusModule(), new EurekaHealthCheckModule(), new AbstractModule() {
			@Override
			protected void configure() {
		        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
				bind(Tracker.class).toInstance(tracker);
				requestInjection(tracker);
			}
		});
		
		Assert.assertEquals(InstanceStatus.STARTING, tracker.initialStatus);
		Assert.assertEquals(InstanceStatus.UP, tracker.handler.getStatus(InstanceStatus.STARTING));
	}
	
	@Test
	public void testFailure() {
		final Tracker tracker = new Tracker();
		Injector injector = null;
		try {
			injector = Governator.createInjector(new ArchaiusModule(), new EurekaHealthCheckModule(), new AbstractModule() {
				@Override
				protected void configure() {
					bind(Tracker.class).toInstance(tracker);
					requestInjection(tracker);
					bind(FailedSingleton.class).asEagerSingleton();
				}
			});
		}
		catch (RuntimeException e) {
			Assert.assertEquals(InstanceStatus.STARTING, tracker.initialStatus);
			Assert.assertEquals(InstanceStatus.OUT_OF_SERVICE, tracker.handler.getStatus(InstanceStatus.STARTING));

		}
	}
	
	@Test
	public void testManualStart() {
	    final Tracker tracker = new Tracker();
        Injector injector = Governator.createInjector(new ArchaiusModule(), new EurekaHealthCheckModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(ApplicationLifecycle.class).to(ManualApplicationLifecycleState.class);
                bind(Tracker.class).toInstance(tracker);
                requestInjection(tracker);
            }
        });
        
        Assert.assertEquals(InstanceStatus.STARTING, tracker.initialStatus);
        Assert.assertEquals(InstanceStatus.STARTING, tracker.handler.getStatus(InstanceStatus.STARTING));
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckHandler handler = injector.getInstance(HealthCheckHandler.class);
        ApplicationLifecycle status = injector.getInstance(ApplicationLifecycle.class);
        
        Assert.assertEquals(LifecycleState.Starting, status.getState());
        Assert.assertEquals(HealthState.Starting, hc.check().join().getState());
        Assert.assertEquals(InstanceStatus.STARTING, handler.getStatus(InstanceStatus.STARTING));

        // Now mark the app as UP
        status.setStarted();
        
        Assert.assertEquals(LifecycleState.Running, status.getState());
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(InstanceStatus.UP, handler.getStatus(InstanceStatus.STARTING));

        // Now mark the app as FAILED
        status.setFailed();
        
        Assert.assertEquals(LifecycleState.Stopped, status.getState());
        Assert.assertEquals(HealthState.OutOfService, hc.check().join().getState());
        Assert.assertEquals(InstanceStatus.OUT_OF_SERVICE, handler.getStatus(InstanceStatus.STARTING));

        // Now mark the app as STOPPED
        status.setStopped();
        
        Assert.assertEquals(LifecycleState.Stopped, status.getState());
        Assert.assertEquals(HealthState.OutOfService, hc.check().join().getState());
        Assert.assertEquals(InstanceStatus.OUT_OF_SERVICE, handler.getStatus(InstanceStatus.STARTING));

	}
}
