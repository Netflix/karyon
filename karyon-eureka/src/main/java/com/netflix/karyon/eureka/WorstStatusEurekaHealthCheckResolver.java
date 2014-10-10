package com.netflix.karyon.eureka;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.karyon.health.HealthCheck;

/**
 * Specialized HealthCheckResolver that will return the first occurance of DOWN
 * or STARTING (in that order) or UP if all status checks are ready.
 * @author elandau
 *
 */
public class WorstStatusEurekaHealthCheckResolver implements EurekaHealthCheckResolver {
    private static final Logger LOG = LoggerFactory.getLogger(WorstStatusEurekaHealthCheckResolver.class);
    
	@Override
	public InstanceStatus resolve(Collection<HealthCheck.Status> statuses) {
		if (statuses != null) {
			for (HealthCheck.Status status : statuses) {
			    LOG.info("Status of '{}' : {} ({})", status.getName(), status.isReady(), status.getError());
			    if (status.hasError()) {
			        return InstanceStatus.DOWN;
			    }
			    if (!status.isReady()) {
			        return InstanceStatus.STARTING;
			    }
			}
		}
		return InstanceStatus.UP;
	}
}
