package com.netflix.karyon.servlet;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.servlet.ServletInfo;

@Singleton
public class ServletAdminResource {
    private ServletInfo info;

    static interface Info {
        Map<String, String> getFilters();
        Map<String, String> getServlets();
    }
    
    @Inject
    public ServletAdminResource(ServletInfo info) {
        this.info = info;
    }
    
    public Info get() {
        return new Info() {
            @Override
            public Map<String, String> getServlets() {
                return info.getServlets();
            }
            
            @Override
            public Map<String, String> getFilters() {
                return info.getFilters();
            }
        };
    }
}
