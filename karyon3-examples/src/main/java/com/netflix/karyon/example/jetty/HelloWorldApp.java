package com.netflix.karyon.example.jetty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.config.MapConfig;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.ProvisionDebugModule;
import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.KaryonFeatures;
import com.netflix.karyon.admin.rest.AdminServerModule;
import com.netflix.karyon.admin.ui.AdminUIServerModule;
import com.netflix.karyon.archaius.ArchaiusKaryon;
import com.netflix.karyon.health.HealthIndicator;
import com.netflix.karyon.log4j.ArchaiusLog4J2ConfigurationModule;
import com.netflix.karyon.rxnetty.shutdown.ShutdownServerModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

@Path("/hello")
public class HelloWorldApp extends DefaultLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldApp.class);
    
    public static void main(String[] args) throws Exception {
        ArchaiusKaryon.bootstrap()
            .withConfigName("helloworld")
            .withApplicationOverrides(MapConfig.builder()
                .put("@appId", "Hello World!")
                .build()
                )
            .addProfile("local")
            .addModules(
                new ArchaiusLog4J2ConfigurationModule(),
                new ProvisionDebugModule(),
                new JettyModule(),
                new AdminServerModule(),
                new AdminUIServerModule(),
                new ShutdownServerModule(),
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        serve("/REST/*").with(GuiceContainer.class);
                        bind(GuiceContainer.class).asEagerSingleton();
                        bind(ArchaiusEndpoint.class).asEagerSingleton();
                        bind(HelloWorldApp.class).asEagerSingleton();
                        bind(HealthIndicator.class).to(FooServiceHealthIndicator.class);
                      
                        bind(LongDelayService.class).asEagerSingleton();
                    }
                  
                    @Override
                    public String toString() {
                        return "JerseyServletModule";
                    }
                }
            )
            .disableFeature(KaryonFeatures.SHUTDOWN_ON_ERROR)
            .start()
            .awaitTermination();
    }
    
    @GET
    public String sayHello() {
        return "hello world";
    }

    @Override
    public void onStarted() {
        LOG.info("HelloWorldApp started");;
    }
}
