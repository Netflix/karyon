package com.netflix.karyon.archaius;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Injector;
import com.netflix.archaius.Config;
import com.netflix.karyon.Karyon;

public class ArchaiusKaryonModuleTest {
    @Test
    public void test() {
        Injector injector = Karyon.create().addProfile("test").start();
        Config config = injector.getInstance(Config.class);
        
        Assert.assertTrue(config.getBoolean("application_loaded", false));
        Assert.assertTrue(config.getBoolean("application_test_loaded", false));
        Assert.assertEquals("application_test", config.getString("application_override"));
    }
}
