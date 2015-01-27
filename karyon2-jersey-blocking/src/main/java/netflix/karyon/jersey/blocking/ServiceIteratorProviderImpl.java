package netflix.karyon.jersey.blocking;

import com.google.common.collect.Iterators;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.service.ServiceFinder;

import java.util.Iterator;

/**
* @author Nitesh Kant
*/
class ServiceIteratorProviderImpl<T> extends ServiceFinder.ServiceIteratorProvider<T> {

    static {
        /**
         * This iterator provider override makes it possible to not mandate the presence of a jar with a META-INF/ based
         * Service provider discovery which is the default for jersey.
         */
        ServiceFinder.setIteratorProvider(new ServiceIteratorProviderImpl());
    }

    @SuppressWarnings("rawtypes")
    private static final Iterator<? extends ContainerProvider> nettyContainerProviderIter =
            Iterators.singletonIterator(new NettyContainerProvider());

    private final ServiceFinder.DefaultServiceIteratorProvider<T> defaultProvider;

    ServiceIteratorProviderImpl() {
        defaultProvider = new ServiceFinder.DefaultServiceIteratorProvider<T>();
    }

    public static void registerWithJersey() {
        // Static block does the register.
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
