package com.netflix.karyon.rxnetty;

import org.junit.Test;

import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.archaius.ArchaiusKaryonConfiguration;

public class RxNettyAdminTest {
    @Test
    public void test() throws InterruptedException {
        Karyon.createInjector(
                ArchaiusKaryonConfiguration.builder()
                    .build(),
                new ArchaiusModule(),
                new RxNettyAdminServerModule()
                )
                .awaitTermination();
    }
}
