package com.netflix.adminresources;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * This class is a minimal simulation of GuiceFilter. Due to the number of
 * statics used in GuiceFilter, there cannot be more than one in an application.
 * The AdminResources app needs minimal features and this class provides those.
 */
@Singleton
class AdminResourcesFilter extends GuiceContainer {
    private final Map<String, HttpServlet> servlets = Maps.newConcurrentMap();
    private volatile String packages;

    @Inject
    AdminResourcesFilter(Injector injector) {
        super(injector);
    }

    /**
     * Set the packages for Jersey to scan for resources
     *
     * @param packages packages to scan
     */
    void setPackages(String packages) {
        this.packages = packages;
    }

    /**
     * Add a non-Jersey servlet mapping
     *
     * @param path path prefix for the servlet
     * @param servlet the servlet
     */
    void mapServlet(String path, HttpServlet servlet) {
        servlets.put(path, servlet);
    }

    @Override
    public int service(URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = (requestUri != null) ? requestUri.getPath() : null;
        if (path != null) {
            for (Map.Entry<String, HttpServlet> entry : servlets.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    entry.getValue().service(request, response);
                    return HttpServletResponse.SC_OK;
                }
            }
        }
        return super.service(baseUri, requestUri, request, response);
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
                                                      WebConfig webConfig) throws ServletException {
        props.put(PackagesResourceConfig.PROPERTY_PACKAGES, packages);
        return new PackagesResourceConfig(props);
    }
}
