package com.netflix.karyon.example.jetty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.admin.internal.SimpleAdminServerModule;
import com.netflix.karyon.archaius.ArchaiusKaryonConfiguration;
import com.netflix.karyon.rxnetty.RxNettyModule;
import com.netflix.karyon.rxnetty.ShutdownServerModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

@Path("/hello")
public class HelloWorldApp extends DefaultLifecycleListener {
    public static void main(String[] args) throws InterruptedException {
        Karyon.createInjector(
            ArchaiusKaryonConfiguration.builder()
                .withConfigName("helloworld")
                .build(),
            new JettyModule(),
            new RxNettyModule(),
            new SimpleAdminServerModule(),
//            new RxNettyAdminServerModule(),
            new ArchaiusModule(),
            new ShutdownServerModule(),
            new JerseyServletModule() {
               @Override
               protected void configureServlets() {
                   serve("/*").with(GuiceContainer.class);
                   
                   bind(GuiceContainer.class).asEagerSingleton();
                   bind(ArchaiusEndpoint.class).asEagerSingleton();
                   
                   bind(HelloWorldApp.class).asEagerSingleton();
               }
               
               @Override
               public String toString() {
                   return "JerseyServletModule";
               }
           }
           )
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
