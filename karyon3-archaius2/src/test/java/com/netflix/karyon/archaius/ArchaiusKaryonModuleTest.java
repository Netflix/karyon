package com.netflix.karyon.archaius;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.netflix.archaius.Config;
import com.netflix.archaius.annotations.ConfigurationSource;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.config.SettableConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.inject.RemoteLayer;
import com.netflix.karyon.Karyon;

public class ArchaiusKaryonModuleTest {
    @ConfigurationSource("foo")
    public static class Foo {
    }
    
    @Test
    public void test() throws ConfigException {
        Injector injector = Karyon.forApplication("test").addModules(new ArchaiusKaryonModule()).addProfile("test").start();
        Config config = injector.getInstance(Config.class);
        
        Assert.assertTrue(config.getBoolean("application_loaded", false));
        Assert.assertTrue(config.getBoolean("application_test_loaded", false));
        Assert.assertEquals("application_test", config.getString("application_override"));
    }
    
    @Test
    public void testApplicationOverrideConfig() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addModules(new ArchaiusKaryonModule()
                    .withApplicationOverrides(singletonConfig("application_test", "code_override"))
                )
                .start();
        
        Config config = injector.getInstance(Config.class);
        assertThat(config.getString("application_test"), equalTo("code_override"));
        
    }
    
    @Test
    public void testApplicationOverrideProperties() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addModules(new ArchaiusKaryonModule()
                    .withApplicationOverrides(singletonProperties("application_test", "code_override"))
                )
                .start();
        
        Config config = injector.getInstance(Config.class);
        assertThat(config.getString("application_test"), equalTo("code_override"));
        
    }
    
    @Test
    public void testLibrariesConfig() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addModules(new ArchaiusKaryonModule())
                .start();
        
        Foo foo = injector.getInstance(Foo.class);
        Config config = injector.getInstance(Config.class);
        
        assertTrue(config.getBoolean("foo_loaded"));
        assertThat(config.getString("foo_override"), equalTo("file"));
    }
    
    @Test
    public void testLibrariesOverrideConfig() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addModules(new ArchaiusKaryonModule()
                    .withLibraryOverrides("foo", singletonConfig("foo_override", "code"))
                )
                .start();
        
        Foo foo = injector.getInstance(Foo.class);
        Config config = injector.getInstance(Config.class);
        
        System.out.println("Value: " + config.getString("foo_override"));
        assertThat(config.getString("foo_override"), equalTo("code"));
    }
    
    @Test
    public void testLibrariesOverrideProperties() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addProfile("local")
                .addModules(new ArchaiusKaryonModule()
                    .withLibraryOverrides("foo", singletonProperties("foo_override", "code"))
                )
                .start();
        
        Foo foo = injector.getInstance(Foo.class);
        Config config = injector.getInstance(Config.class);
        assertThat(config.getString("foo_override"), equalTo("code"));
    }
    
    @Test
    public void testMockRemoteConfig() throws ConfigException {
        Injector injector = Karyon.newBuilder()
                .addModules(new ArchaiusKaryonModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Key.get(Config.class, RemoteLayer.class)).toProvider(new Provider<Config>() {
                            @Inject
                            @Raw
                            Config rawConfig;
                            
                            @Override
                            public Config get() {
                                final SettableConfig remote = new DefaultSettableConfig();
                                remote.setProperty("foo", "foo-" + rawConfig.getString("application_loaded"));
                                return remote;
                            }
                        });
                    }
                })
                .start();
        
        Foo foo = injector.getInstance(Foo.class);
        Config config = injector.getInstance(Config.class);
        
        assertThat(config.getString("foo"), equalTo("foo-true"));
    }
    
    Properties singletonProperties(String key, String value) {
        Properties props = new Properties();
        props.put(key, value);
        return props;
    }
    
    Config singletonConfig(String key, String value) {
        return MapConfig.builder()
        .put(key, value)
        .build();
    }
}
