package com.netflix.karyon.example.rxnetty;

import javax.inject.Singleton;

import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.auto.ModuleListProviders;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.archaius.ArchaiusKaryonConfiguration;
import com.netflix.karyon.rxnetty.RxNettyModule;

@Singleton
public class RxNettyHelloWorldApp extends DefaultLifecycleListener {
    public static void main(String[] args) throws InterruptedException {
        Karyon.createInjector(
            ArchaiusKaryonConfiguration.builder()
                .withConfigName("rxnetty-helloworld")
                .addModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"))
                .build(),
            new RxNettyModule(),               // Needed to start the RxNetty servers
            new ShutdownServerModule(),
            new HelloWorldEndpointModule()
            )
            .awaitTermination();
    }
}
