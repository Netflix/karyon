package com.netflix.karyon.jetty;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.guice.jetty.JettyConfig;
import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.conditional.ConditionalOnModule;
import com.netflix.karyon.conditional.OverrideModule;

@OverrideModule
@ConditionalOnModule(JettyModule.class)
public final class JettyOverrideModule extends DefaultModule {
    @Provides
    @Singleton
    private JettyConfig getDefaultConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AnnotatedJettyConfig.class);
    }
}
