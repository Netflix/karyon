package com.netflix.karyon;

import org.junit.Test;

public class KaryonTest {
    @Test
    public void testDefaultWithoutArchaius() {
        Karyon
            .create()
            .disableFeature(KaryonFeatures.USE_ARCHAIUS)
            .start();
    }
}
