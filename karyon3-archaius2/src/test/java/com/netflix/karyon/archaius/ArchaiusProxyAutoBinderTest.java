package com.netflix.karyon.archaius;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.karyon.Karyon;

public class ArchaiusProxyAutoBinderTest {
    @Configuration
    public static interface Foo {
        int timeout();
    }
    
    @Singleton
    public static class Bar {
        @Inject
        Bar(Foo foo) {
            
        }
    }
    
    @Test
    public void testAutoBinder() throws ConfigException {
        Injector injector = Karyon.newBuilder()
            .addModules(new ArchaiusKaryonModule().withApplicationOverrides(MapConfig.builder()
                    .put("timeout", 123)
                    .build()
                    ))
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Bar.class);
                }
            })
            .start();
        
        Foo foo = injector.getInstance(Foo.class);
        assertThat(foo.timeout(), equalTo(123));
    }
}
