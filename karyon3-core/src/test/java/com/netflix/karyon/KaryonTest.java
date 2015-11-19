package com.netflix.karyon;

import org.junit.Test;

import com.netflix.karyon.api.KaryonFeatures;

public class KaryonTest {
    @Test(expected=RuntimeException.class)
    public void testDefault() {
        Karyon.create().start();
    }
    
    @Test
    public void testDefaultWithoutArchaius() {
        Karyon
            .create()
            .disableFeature(KaryonFeatures.USE_ARCHAIUS)
            .start();
    }
}
