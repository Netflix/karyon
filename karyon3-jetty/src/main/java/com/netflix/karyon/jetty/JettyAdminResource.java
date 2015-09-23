package com.netflix.karyon.jetty;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.guice.jetty.JettyConfig;

@Singleton
public class JettyAdminResource {
    private final JettyConfig jettyConfig;
    
    @Inject
    public JettyAdminResource(JettyConfig jettyConfig) {
        this.jettyConfig = jettyConfig;
    }
    
    public JettyConfig get() {
        return jettyConfig;
    }
}
