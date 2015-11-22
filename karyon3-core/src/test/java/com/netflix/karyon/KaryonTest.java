package com.netflix.karyon;

import org.junit.Test;

public class KaryonTest {
    @Test
    public void defaultKaryonSucceeds() {
        Karyon
            .create()
            .start();
    }
}
