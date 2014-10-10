package com.netflix.karyon.eureka;

import com.google.inject.ImplementedBy;
import com.netflix.appinfo.InstanceInfo;

/**
 * @author Nitesh Kant
 */
@Deprecated
@ImplementedBy(DefaultEurekaKaryonStatusBridge.class)
public interface EurekaKaryonStatusBridge {

    InstanceInfo.InstanceStatus interpretKaryonStatus(int karyonStatus);
}
