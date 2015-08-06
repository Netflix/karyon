package com.netflix.karyon.eureka;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.Governator;

import junit.framework.Assert;

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
		Injector injector = Governator.createInjector(new ArchaiusModule(), new AbstractModule() {
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
			injector = Governator.createInjector(new ArchaiusModule(), new AbstractModule() {
				@Override
				protected void configure() {
			        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
					bind(Tracker.class).toInstance(tracker);
					requestInjection(tracker);
					bind(FailedSingleton.class).asEagerSingleton();
				}
			});
		}
		catch (RuntimeException e) {
			Assert.assertEquals(InstanceStatus.STARTING, tracker.initialStatus);
			Assert.assertEquals(InstanceStatus.DOWN, tracker.handler.getStatus(InstanceStatus.STARTING));

		}
	}
}
