package com.netflix.karyon.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.ModuleListProviders;
import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.Karyon;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

@Path("/")
public class HelloWorldApp extends DefaultLifecycleListener {
    public static void main(String[] args) throws InterruptedException {
        Karyon
            .forModules(
                 new JettyModule(),
                 new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        serve("/*").with(GuiceContainer.class);
                        bind(GuiceContainer.class).asEagerSingleton();
                        
                        bind(HelloWorldApp.class).asEagerSingleton();
                    }  
                }
            )
            .withModuleFinder(ModuleListProviders.forPackages("com.netflix.karyon"))
            .withConfigName("helloworld")
            .withBootstrapModule(new ArchaiusModule())
            .create()
            .awaitTermination();
    }
    
    @GET
    public String sayHello() {
        return "hello world";
    }

    @Override
    public void onStarted() {
        System.out.println("Started ***** ");
    }
}
