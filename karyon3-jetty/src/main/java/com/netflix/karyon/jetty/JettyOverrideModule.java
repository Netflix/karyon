package com.netflix.karyon.jetty;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.governator.auto.annotations.OverrideModule;
import com.netflix.governator.guice.jetty.JettyConfig;
import com.netflix.governator.guice.jetty.JettyModule;

@OverrideModule(JettyModule.class)
@ConditionalOnModule(JettyModule.class)
public class JettyOverrideModule extends DefaultModule {
    @Provides
    @Singleton
    private JettyConfig getDefaultConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AnnotatedJettyConfig.class);
    }
}
