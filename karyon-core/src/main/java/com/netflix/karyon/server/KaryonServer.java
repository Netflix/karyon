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

package com.netflix.karyon.server;

import com.netflix.karyon.server.bootstrap.DelegatingBootstrapWithDefaults;
import com.netflix.karyon.server.bootstrap.KaryonBootstrap;
import com.netflix.karyon.server.bootstrap.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * The core server for Karyon. This server provides all core functionality to startup a karyon server but is not useful
 * as is, since, it does not essentially open an endpoint for clients to connect. <br/>
 * In order to use karyon effectively, one should use the provided extensions in karyon-extensions <p/>
 *
 * <h2>Example</h2>
 * One can create a very simple server using karyon as:
 *
 <PRE>
 public class MyServer {

     public static void main(String[] args) throws Exception {
         KaryonBootstrap bootstrap = new DefaultBootstrap.Builder("myapp", "dev").build();
         final KaryonServer server = new KaryonServer(bootstrap);
         server.start();

         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                public void run() {
                    try {
                        server.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));

         ServerSocket serverSocket = new ServerSocket(80);
         while (true) {
         final Socket s = serverSocket.accept();
         // Process request
         }
     }
}
 </PRE>
 *
 * @author Nitesh Kant
 */
public class KaryonServer {

    protected static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    protected final KaryonBootstrap bootstrap;

    public KaryonServer() {
        this(null);
    }

    public KaryonServer(@Nullable KaryonBootstrap bootstrap) {
        if (null == bootstrap) {
            this.bootstrap = DelegatingBootstrapWithDefaults.DEFAULT_WHEN_NULL;
        } else {
            this.bootstrap = new DelegatingBootstrapWithDefaults(bootstrap);
        }
    }

    public void start() throws Exception {
        bootstrap.bootstrap();
        internalStart();
        ServiceRegistryClient serviceRegistryClient = bootstrap.serviceRegistryClient();
        if (null != serviceRegistryClient) {
            serviceRegistryClient.updateStatus(ServiceRegistryClient.ServiceStatus.UP);
        }
    }

    /**
     * Stops the karyon server. <br/>
     * This typically should be called at termination of the server.
     *
     * @throws Exception If the stop failed.
     */
    public void stop() throws Exception {
        ServiceRegistryClient serviceRegistryClient = bootstrap.serviceRegistryClient();
        if (null != serviceRegistryClient) {
            serviceRegistryClient.updateStatus(ServiceRegistryClient.ServiceStatus.DOWN);
        }
        internalStop();
    }

    /**
     * Called from within {@link #start()} after calling {@link KaryonBootstrap#bootstrap()} and before calling
     * {@link ServiceRegistryClient#updateStatus(ServiceRegistryClient.ServiceStatus)} with status
     * {@link ServiceRegistryClient.ServiceStatus#UP}. <br/>
     * By default this method does not do anything.
     */
    protected void internalStart() throws Exception {
    }

    /**
     * Called from within {@link #stop()} after calling
     * {@link ServiceRegistryClient#updateStatus(ServiceRegistryClient.ServiceStatus)} with status
     * {@link ServiceRegistryClient.ServiceStatus#DOWN}. <br/>
     * By default this method does not do anything.
     */
    protected void internalStop() throws Exception {
    }
}
