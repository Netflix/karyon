package com.netflix.karyon.conditional;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Injector;
import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.Governator;

public class ConditionalOnLocalDevTest {
    @Test
    public void test() {
        Injector injector = Governator.createInjector(DefaultGovernatorConfiguration.builder().build());
        OnLocalDevCondition condition = injector.getInstance(OnLocalDevCondition.class);
        Assert.assertTrue(condition.check(null));
    }
}
