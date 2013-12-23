package com.netflix.karyon.server.http.jersey.blocking;

import com.google.common.collect.Iterators;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.service.ServiceFinder;

import java.util.Iterator;

/**
 * @author Nitesh Kant
 */
public final class JerseyRouterProvider {

    private JerseyRouterProvider() {
    }

    public static HttpRequestRouter createRouter() {
        /**
         * This iterator provider override makes it possible to not mandate the presence of a jar with a META-INF/ based
         * Service provider discovery which is the default for jersey.
         */
        ServiceFinder.setIteratorProvider(new ServiceIteratorProviderImpl());
        return new JerseyBasedRouter(new PropertiesBasedResourceConfig());
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
            // TODO: Not sure if we need any change in this for our usecase.
            return defaultProvider.createClassIterator(service, serviceName, loader, ignoreOnClassNotFound);
        }
    }
}
