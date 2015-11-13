package com.netflix.karyon.jetty;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.guice.jetty.JettyConfig;
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="jetty", index="config")
public class JettyAdminResource {
    private final JettyConfig jettyConfig;
    
    @Inject
    public JettyAdminResource(JettyConfig jettyConfig) {
        this.jettyConfig = jettyConfig;
    }
    
    public JettyConfig config() {
        return jettyConfig;
    }
}
