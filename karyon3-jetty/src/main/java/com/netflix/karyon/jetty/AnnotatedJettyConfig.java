package com.netflix.karyon.jetty;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.governator.guice.jetty.JettyConfig;

@Configuration(prefix="karyon.jetty")
public interface AnnotatedJettyConfig extends JettyConfig {
    @DefaultValue("8080")
    int getPort();
}
