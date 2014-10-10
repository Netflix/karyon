package com.netflix.karyon.eureka;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.karyon.health.HealthCheck;

/**
 * StatusCheck based on the ApplicationInfoManager tracked status.
 * 
 * @author elandau
 */
@Singleton
public class ApplicationInfoManagerHealthCheck implements HealthCheck {
    ApplicationInfoManager manager;
    
	@Deprecated
	public ApplicationInfoManagerHealthCheck() {
	    manager = ApplicationInfoManager.getInstance();
	}
	
	@Inject
	public ApplicationInfoManagerHealthCheck(ApplicationInfoManager manager) {
		this.manager = manager;
	}
	
    @Override
    public Status check() {
        InstanceStatus status = manager.getInstanceStatus();
        if (status != null) {
            switch (status) {
            case UP:
                return Status.ready(this);
            case STARTING:
                return Status.error(this, null);
            case DOWN:
                return Status.error(this, new Exception("Application is DOWN"));
            default:
                return Status.error(this, new Exception("Invalid state : " + status));
            }
        }
        else {
            return Status.error(this, new Exception("Invalid status <null>"));
        }
    }
    
    @Override
    public String getName() {
        return "ApplicationInfoManager";
    }
    
    @Override
    public String toString() {
        return "ApplicationInfoManagerHealthCheck []";
    }

}
