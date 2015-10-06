package com.netflix.karyon.conditional;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Injector;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.KaryonFeatures;
import com.netflix.karyon.conditional.impl.OnLocalDevCondition;

public class ConditionalOnLocalDevTest {
    @Test
    public void test() throws Exception {
        Injector injector = Karyon.create().disableFeature(KaryonFeatures.USE_ARCHAIUS).start();
        OnLocalDevCondition condition = injector.getInstance(OnLocalDevCondition.class);
        Assert.assertTrue(condition.check(null));
    }
}
