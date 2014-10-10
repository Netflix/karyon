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

package com.netflix.karyon.eureka;

import javax.inject.Inject;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.karyon.health.HealthCheckInvoker;
import com.netflix.karyon.health.HealthCheckRegistry;

/**
 * @author Nitesh Kant
 */
public class EurekaHealthCheckHandler implements com.netflix.appinfo.HealthCheckHandler {

    private final HealthCheckRegistry registry;
    private HealthCheckInvoker invoker;
    private EurekaHealthCheckResolver resolver;

    @Inject
    public EurekaHealthCheckHandler(HealthCheckRegistry registry, HealthCheckInvoker invoker, EurekaHealthCheckResolver resolver) {
        this.registry = registry;
        this.invoker = invoker;
        this.resolver = resolver;
    }

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus currentStatus) {
        return resolver.resolve(invoker.invoke(registry.getHealthChecks()));
    }
}
