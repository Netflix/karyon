package com.netflix.karyon.server.http.jersey.blocking;

import com.google.common.collect.Iterators;
import com.google.inject.Injector;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.service.ServiceFinder;

import java.util.Iterator;

/**
 * @author Nitesh Kant
 */
public final class JerseyRouterProvider {

    static {
        /**
         * This iterator provider override makes it possible to not mandate the presence of a jar with a META-INF/ based
         * Service provider discovery which is the default for jersey.
         */
        ServiceFinder.setIteratorProvider(new ServiceIteratorProviderImpl());
    }

    private JerseyRouterProvider() {
    }

    public static JerseyBasedRouter createRouter() {
        return new JerseyBasedRouter(new PropertiesBasedResourceConfig());
    }

    public static JerseyBasedRouter createRouter(Injector injector) {
        return new JerseyBasedRouter(new PropertiesBasedResourceConfig(), injector);
    }

    private static class ServiceIteratorProviderImpl<T> extends ServiceFinder.ServiceIteratorProvider<T> {

        @SuppressWarnings("rawtypes")
        private static final Iterator<? extends ContainerProvider> nettyContainerProviderIter =
                Iterators.singletonIterator(new NettyContainerProvider());

        private final ServiceFinder.DefaultServiceIteratorProvider<T> defaultProvider;

        private ServiceIteratorProviderImpl() {
            defaultProvider = new ServiceFinder.DefaultServiceIteratorProvider<T>();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<T> createIterator(Class<T> service, String serviceName, ClassLoader loader,
                                          boolean ignoreOnClassNotFound) {
            Iterator<T> defaultIterator = defaultProvider.createIterator(service, serviceName, loader, ignoreOnClassNotFound);
            if (service.isAssignableFrom(NettyContainerProvider.class)) {
                return (Iterator<T>) Iterators.concat(defaultIterator, nettyContainerProviderIter);
            }
            return defaultIterator;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<Class<T>> createClassIterator(Class<T> service, String serviceName, ClassLoader loader,
                                                      boolean ignoreOnClassNotFound) {
            return defaultProvider.createClassIterator(service, serviceName, loader, ignoreOnClassNotFound);
        }
    }
}
