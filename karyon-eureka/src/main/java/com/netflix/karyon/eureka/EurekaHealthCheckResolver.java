package com.netflix.karyon.eureka;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.karyon.health.HealthCheck;

/**
 * SPI for strategy to resolve a set of {@link HealthCheck.Status}'s into a single 
 * {@link HealthCheck.Status}
 * @author elandau
 */
@ImplementedBy(WorstStatusEurekaHealthCheckResolver.class)
public interface EurekaHealthCheckResolver {
	InstanceStatus resolve(Collection<HealthCheck.Status> statuses);
}
