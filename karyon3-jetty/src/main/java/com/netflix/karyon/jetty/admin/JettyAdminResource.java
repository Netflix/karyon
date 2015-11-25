package com.netflix.karyon.jetty.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.admin.AdminService;
import com.netflix.karyon.jetty.JettyConfig;

@Singleton
@AdminService(name="jetty", index="config")
final class JettyAdminResource {
    private final JettyConfig jettyConfig;
    
    @Inject
    public JettyAdminResource(JettyConfig jettyConfig) {
        this.jettyConfig = jettyConfig;
    }
    
    public JettyConfig config() {
        return jettyConfig;
    }
}
