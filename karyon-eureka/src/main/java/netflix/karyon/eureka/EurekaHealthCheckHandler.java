/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package netflix.karyon.eureka;

import com.netflix.appinfo.InstanceInfo;
import netflix.karyon.health.HealthCheckHandler;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
public class EurekaHealthCheckHandler implements com.netflix.appinfo.HealthCheckHandler {

    private final HealthCheckHandler healthCheckHandler;
    private final EurekaKaryonStatusBridge eurekaKaryonStatusBridge;

    @Inject
    public EurekaHealthCheckHandler(HealthCheckHandler healthCheckHandler,
                                    EurekaKaryonStatusBridge eurekaKaryonStatusBridge) {
        this.healthCheckHandler = healthCheckHandler;
        this.eurekaKaryonStatusBridge = eurekaKaryonStatusBridge;
    }

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus currentStatus) {
        int healthStatus = healthCheckHandler.getStatus();
        return eurekaKaryonStatusBridge.interpretKaryonStatus(healthStatus);
    }
}
