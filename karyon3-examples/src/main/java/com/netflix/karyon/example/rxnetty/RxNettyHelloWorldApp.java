package com.netflix.karyon.example.rxnetty;

import javax.inject.Singleton;

import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.admin.rest.AdminServerModule;
import com.netflix.karyon.admin.ui.AdminUIServerModule;
import com.netflix.karyon.archaius.ArchaiusKaryonConfiguration;
import com.netflix.karyon.rxnetty.server.RxNettyServerModule;
import com.netflix.karyon.rxnetty.shutdown.ShutdownServerModule;

@Singleton
public class RxNettyHelloWorldApp extends DefaultLifecycleListener {
    public static void main(String[] args) throws InterruptedException, ConfigException {
        Karyon.createInjector(
            ArchaiusKaryonConfiguration.builder()
                .withConfigName("rxnetty-helloworld")
                .withApplicationOverrides(MapConfig.builder()
                        .put("@serverId", "localhost")
                        .build()
                        )
                .build(),
            new ArchaiusModule(),
            new AdminServerModule(),
            new AdminUIServerModule(),
            // These bindings will go on the 'default' server
            new RxNettyServerModule() {
                @Override
                protected void configureEndpoints() {
                    serve("/hello").with(HelloWorldRequestHandler.class);
                }
            },
            // These bindings will go on the 'HelloServer' server
            new RxNettyServerModule() {
                @Override
                protected void configureEndpoints() {
                    serve(FooBarServer.class, "/foo").with(FooRequestHandler.class);
                    serve(FooBarServer.class, "/bar").with(BarRequestHandler.class);
                }
            },
            new ShutdownServerModule()
            )
            .awaitTermination();
    }
}
