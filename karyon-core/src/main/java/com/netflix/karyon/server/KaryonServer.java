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

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.server.lifecycle.ServerInitializer;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * The core server for Karyon. This server provides all core functionality to startup a karyon server but is not useful
 * as is, since, it does not essentially open an endpoint for clients to connect. <br/>
 * In order to use karyon effectively, one should use the provided extensions in karyon-extensions <p/>
 *
 * If one intends to provide a custom extension the following needs to be done:
 * <ul>
 <li>Call {@link com.netflix.karyon.server.KaryonServer#initialize()} at startup to bootstrap <a href="https://github.com/Netflix/governator/">Governator</a>.
 The bootstrapping is done via {@link ServerBootstrap}. One can customize various aspects of governator bootstrapping
 by providing an extension to {@link ServerBootstrap} and overriding various methods therein. In such a case, fully
 qualified, custom bootstrap class name, must be specified as a property
 {@link com.netflix.karyon.spi.PropertyNames#SERVER_BOOTSTRAP_CLASS_OVERRIDE} available to archaius. Such a class must
 also have a no-arg public constructor.</li>
 <li>Call {@link com.netflix.karyon.server.KaryonServer#start()} to initialize the karyon environment and hence
 initializing all classes annotated with {@link com.netflix.karyon.spi.Application} and {@link com.netflix.karyon.spi.Component}</li>
 <li>Call {@link com.netflix.karyon.server.KaryonServer#close()} at shutdown or to stop the server. This will cause
 destruction of all governator initialized classes. </li>
 </ul>
 *
 * <h2>Eureka Integration</h2>
 * Karyon provides integration with eureka as a core component and does the necessary registration/update with eureka.
 * In order for the integration to work, the application writer, must provide the necessary configuration for eureka
 * client as specified in the "Configuring Eureka Client" section <a href="https://github.com/Netflix/eureka/wiki/Configuring-Eureka">here</a>
 * The easiest way of doing so is specifying a property file with name "eureka-client.properties" in the classpath.
 * This file must have the properties required by eureka with a prefix "eureka", eg: if you need to specify the VIP
 * address for eureka you must specify the property "eureka.vipAddress". If you need to use a different prefix, the
 * default prefix can be overridden by specifying the same in the property {@link PropertyNames#EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME}
 * as a property accessible to <a href="https://github.com/Netflix/archaius">archaius</a>
 * The integration with eureka can be disabled by specifying a property
 * {@link com.netflix.karyon.spi.PropertyNames#DISABLE_EUREKA_INTEGRATION} set to <code>true</code> and accessible to
 * <a href="https://github.com/Netflix/archaius">archaius</a>
 *
 * <h2>Archaius Integration</h2>
 * As such <a href="https://github.com/Netflix/archaius/">Archaius</a> requires minimal integration and works out of the
 * box however, karyon does one additional step of loading the configuration from properties file located in the
 * classpath. The properties loading is done by calling {@link ConfigurationManager#loadCascadedPropertiesFromResources(String)}
 * with the name of the config as returned by {@link com.netflix.config.DeploymentContext#getApplicationId()} from the
 * deployment context configured in archaius.
 * For the above to work correctly, one must provide a property with name "archaius.deployment.applicationId" to
 * archaius before karyon is initialized i.e. before calling {@link com.netflix.karyon.server.KaryonServer#initialize()}.
 * The value of the property must be the name of the application. By doing the above, archaius loads all properties
 * defined in properties file(s) having the names:
 *
 * [application_name].properties
 * [application_name]-[environement].properties
 *
 * The environment above is as retrieved from {@link com.netflix.config.DeploymentContext#getApplicationId()} from the
 * deployment context configured in archaius. This can be set by a property "archaius.deployment.environment"
 *
 * NOTE: The above property names are valid if the default deployment context is used for archaius.
 *
 * If any customization is required in archaius, one should do the same before instantiating
 * {@link com.netflix.karyon.server.KaryonServer}.
 *
 * If this archaius integration is not required, you must set a system property
 * {@link PropertyNames#DISABLE_ARCHAIUS_INTEGRATION}
 *
 * <h2>Example</h2>
 * One can create a very simple server using karyon as:
 *
 <PRE>
 public class MyServer {

     public static void main(String[] args) throws Exception {
         final KaryonServer server = new KaryonServer();
         server.initialize();
         server.start();

         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                public void run() {
                    try {
                        server.close();
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
public class KaryonServer implements Closeable {

    protected static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    private LifecycleManager lifecycleManager;
    private Injector injector;
    private ServerInitializer initializer;
    private ServerBootstrap serverBootstrap;

    /**
     * Same as calling {@link KaryonServer#KaryonServer(ServerBootstrap)} with <code>null</code>  argument.
     */
    public KaryonServer() {
        this(null);
    }

    /**
     * Instantiates karyon server with the passed bootstrap class. If the passed bootstrap class is <code>null</code>
     * then the bootstrap class name is obtained from the property {@link PropertyNames#SERVER_BOOTSTRAP_CLASS_OVERRIDE},
     * absence of which, uses {@link ServerBootstrap}.
     *
     * @param bootstrap Bootstrap class to use, can be null.
     */
    public KaryonServer(@Nullable ServerBootstrap bootstrap) {

        PhaseInterceptorRegistry.notifyInterceptors(InitializationPhaseInterceptor.Phase.OnCreate);

        if (null == bootstrap) {
            String bootstrapClassName =
                    ConfigurationManager.getConfigInstance().getString(PropertyNames.SERVER_BOOTSTRAP_CLASS_OVERRIDE);
            if (null == bootstrapClassName) {
                serverBootstrap = new ServerBootstrap();
            } else {
                serverBootstrap = instantiateBootstrapClass(bootstrapClassName);
            }
        } else {
            serverBootstrap = bootstrap;
        }

        PhaseInterceptorRegistry.notifyInterceptors(InitializationPhaseInterceptor.Phase.InitBootstrap);

        serverBootstrap.initialize();
    }

    /**
     * Bootstraps karyon by using {@link ServerBootstrap} by default. This can be customized by extending
     * {@link ServerBootstrap}. <br/>
     * This method must be called during the server initialization.
     *
     * @return Guice's injector instance that will be used by <a href="https://github.com/Netflix/governator/">Governator</a>
     */
    public synchronized Injector initialize() {

        if (null != injector) {
            return injector;
        }

        LifecycleInjectorBuilder injectorBuilder = serverBootstrap.bootstrap();
        serverBootstrap.beforeInjectorCreation(injectorBuilder);

        injector = injectorBuilder.createInjector();

        initializer = injector.getInstance(ServerInitializer.class);

        return injector;
    }

    /**
     * Starts the karyon server by starting <a href="https://github.com/Netflix/governator/">Governator</a>
     * and calling {@link ServerInitializer#initialize(com.google.inject.Injector)} for the applications, components to be
     * started inside karyon. <br/>
     * This typically should be called at startup.
     *
     * @throws Exception If startup fails for any reason.
     */
    public void start() throws Exception {

        lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();

        initializer.initialize(injector);
    }

    /**
     * Stops the karyon server by stopping <a href="https://github.com/Netflix/governator/">Governator</a> and
     * {@link com.netflix.karyon.server.lifecycle.ServerInitializer#close()}. <br/>
     * This typically should be called at termination of the server.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        logger.info("Shutting down karyon.");
        if (null != initializer) {
            initializer.close();
        }
        Closeables.closeQuietly(lifecycleManager);
        logger.info("Successfully shut down karyon.");
    }

    private ServerBootstrap instantiateBootstrapClass(String bootstrapClassName) {
        try {
            return (ServerBootstrap) Class.forName(bootstrapClassName).newInstance();
        } catch (InstantiationException e) {
            logger.error(String.format("Failed to instantiate server bootstrap class %s", bootstrapClassName), e);
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            logger.error(String.format("Access bootstrap class %s is not allowed.", bootstrapClassName), e);
            throw Throwables.propagate(e);
        } catch (ClassNotFoundException e) {
            logger.error(String.format("Bootstrap class %s not found.", bootstrapClassName), e);
            throw Throwables.propagate(e);
        } catch (ClassCastException e) {
            logger.error(String.format("Bootstrap class %s should extend from %s", bootstrapClassName, ServerBootstrap.class.getName()), e);
            throw Throwables.propagate(e);
        }
    }
}
