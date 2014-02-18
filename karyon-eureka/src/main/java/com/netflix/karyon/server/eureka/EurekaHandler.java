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

package com.netflix.karyon.server.eureka;

import com.google.common.base.Preconditions;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.CloudInstanceConfig;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.karyon.server.bootstrap.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.bootstrap.HealthCheckInvocationStrategy;
import com.netflix.karyon.server.bootstrap.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A handler for integrating with <a href="https://github.com/Netflix/eureka/">Eureka</a>. <br/>
 *
 * @author Nitesh Kant
 */
public class EurekaHandler implements ServiceRegistryClient {

    protected static final Logger logger = LoggerFactory.getLogger(EurekaHandler.class);

    private EurekaHealthCheckCallback eurekaHealthCheckCallback;
    private HealthCheckInvocationStrategy healthCheckInvocationStrategy;

    private AtomicBoolean registered = new AtomicBoolean();

    protected final String eurekaNamespace;
    protected final String eurekaClientNamespace;
    protected final DataCenterInfo.Name datacenterType;

    public EurekaHandler(EurekaHealthCheckCallback eurekaHealthCheckCallback,
                         HealthCheckInvocationStrategy healthCheckInvocationStrategy,
                         @Nullable String eurekaNamespace, @Nullable String eurekaClientNamespace,
                         @Nullable DataCenterInfo.Name datacenterType) {
        this.eurekaNamespace = sanitizeNamespace(null == eurekaNamespace ? "eureka" : eurekaNamespace);
        this.eurekaClientNamespace = null == eurekaClientNamespace ? eurekaNamespace : sanitizeNamespace(eurekaClientNamespace);
        this.datacenterType = null == datacenterType ? DataCenterInfo.Name.MyOwn : datacenterType;
        this.eurekaHealthCheckCallback = eurekaHealthCheckCallback;
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
    }

    @Override
    public void start() {
        register();
    }

    protected EurekaInstanceConfig createEurekaInstanceConfig() {
        EurekaInstanceConfig eurekaInstanceConfig;

        switch (datacenterType) {
            case Amazon:
                eurekaInstanceConfig = new CloudInstanceConfig(eurekaNamespace);
                break;
            case Netflix:
                eurekaInstanceConfig = new MyDataCenterInstanceConfig(eurekaNamespace, new DataCenterInfo() {
                    @Override
                    public Name getName() {
                        return Name.Netflix;
                    }
                });
                break;
            default:
                // Every other value is just custom data center.
                eurekaInstanceConfig = new MyDataCenterInstanceConfig(eurekaNamespace);
                break;
        }
        return eurekaInstanceConfig;
    }

    public void markAsUp() {
        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    public void markAsDown() {
        DiscoveryManager.getInstance().shutdownComponent();
        if (null != healthCheckInvocationStrategy
                && AsyncHealthCheckInvocationStrategy.class.isAssignableFrom(healthCheckInvocationStrategy.getClass())) {
            try {
                ((AsyncHealthCheckInvocationStrategy) healthCheckInvocationStrategy).stop();
            } catch (InterruptedException e) {
                Thread.interrupted(); // reset the interrupted status
                logger.error("Interrupted while stopping the async health check invocation strategy. Ignoring.", e);
            }
        }
    }

    @Override
    public void updateStatus(ServiceStatus newStatus) {
        Preconditions.checkNotNull(newStatus, "Service status can not be null.");

        switch (newStatus) {
            case UP:
                markAsUp();
                break;
            case DOWN:
                markAsDown();
                break;
        }
    }

    private static String sanitizeNamespace(String providedNamespace) {
        if (!providedNamespace.endsWith(".")) {
            return providedNamespace + '.';
        }
        return providedNamespace;
    }

    protected void register() {
        if (!registered.compareAndSet(false, true)) {
            logger.info("Eureka handler already registered, skipping registration.");
            return;
        }

        EurekaInstanceConfig eurekaInstanceConfig = createEurekaInstanceConfig();

        DiscoveryManager.getInstance().initComponent(eurekaInstanceConfig, new DefaultEurekaClientConfig(eurekaClientNamespace));
        if (null != eurekaHealthCheckCallback) {
            // We always register the callback with eureka, the handler in turn checks if the unification is enabled, if yes,
            // the underlying handler is used else returns healthy.
            DiscoveryManager.getInstance().getDiscoveryClient().registerHealthCheckCallback(eurekaHealthCheckCallback);
        }
    }

    public static class Builder {

        private String namespace;
        private String clientNamespace;
        private DataCenterInfo.Name datacenterType;
        private final HealthCheckInvocationStrategy strategy;
        private final EurekaHealthCheckCallback callback;

        public Builder(HealthCheckInvocationStrategy strategy) {
            Preconditions.checkNotNull(strategy, "Health check strategy can not be null.");
            this.strategy = strategy;
            callback = new EurekaHealthCheckCallback(strategy);
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder clientNamespace(String clientNamespace) {
            this.clientNamespace = clientNamespace;
            return this;
        }

        public Builder datacenterType(DataCenterInfo.Name datacenterType) {
            this.datacenterType = datacenterType;
            return this;
        }

        public EurekaHandler build() {
            return new EurekaHandler(callback, strategy, namespace, clientNamespace, datacenterType);
        }
    }
}
