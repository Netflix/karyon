package com.netflix.karyon.conditional;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.Injector;
import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.Governator;

public class ConditionalOnLocalDevTest {
    @Test
    public void test() throws Exception {
        Injector injector = Governator.createInjector(DefaultGovernatorConfiguration.createDefault());
        OnLocalDevCondition condition = injector.getInstance(OnLocalDevCondition.class);
        Assert.assertTrue(condition.check(null));
    }
}
