package com.netflix.karyon.server.governator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.netflix.governator.guice.lazy.LazySingleton;

/**
 * Default no-op implementation of a ServletContextListener within the context of
 * Guice.
 * 
 * @author elandau
 *
 */
@LazySingleton
public final class DefaultServletContextListener implements ServletContextListener {
    // Called after injector is created and all eager singletons created.  
    @Override
    public void contextInitialized(final ServletContextEvent event) {
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
    }
}
