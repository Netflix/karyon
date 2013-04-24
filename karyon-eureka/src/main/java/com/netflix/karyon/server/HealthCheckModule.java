package com.netflix.karyon.server;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.eureka.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.DefaultHealthCheckHandler;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A guice module that configures only the implementations of {@link HealthCheckHandler} and
 * {@link HealthCheckInvocationStrategy} based on the defined properties. This can be used as a standalone module if
 * only health check implementations are to be bound.
 *
 * @author Nitesh Kant
 */
public class HealthCheckModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckModule.class);

    @Override
    protected void configure() {

        bindHealthCheckStrategy();

        bindHealthCheckHandler();
    }

    protected void bindHealthCheckStrategy() {
        bindACustomClass(binder(), PropertyNames.HEALTH_CHECK_STRATEGY,
                HealthCheckInvocationStrategy.class,
                "No health check invocation strategy specified, using the default strategy %s. In order to override " +
                "this behavior you provide an implementation of %s and specify the fully qualified class name of " +
                "the implementation in the property %s", AsyncHealthCheckInvocationStrategy.class.getName(),
                HealthCheckInvocationStrategy.class.getName(), PropertyNames.HEALTH_CHECK_STRATEGY);
    }

    protected void bindHealthCheckHandler() {
        bindACustomClass(binder(), PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                HealthCheckHandler.class,
                "No health check handler defined. This means your application can not provide meaningful health " +
                "state to external entities. It is highly recommended that you provide an implementation of %s and " +
                "specify the fully qualified class name of the implementation in the property %s",
                HealthCheckHandler.class.getName(), PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean bindACustomClass(Binder binder, String customClassPropName, Class<T> bindTo,
                                         String propertNotFoundErrMsg, Object... arguments) {
        boolean bound = false;
        String customClassName = ConfigurationManager.getConfigInstance().getString(customClassPropName);
        if (null != customClassName) {
            Class<? extends T> customClass = null;
            try {
                Class<?> aClass = Class.forName(customClassName);
                if (bindTo.isAssignableFrom(aClass)) {
                    binder.bind(bindTo).to((Class<? extends T>) aClass);
                    bound = true;
                } else {
                    logger.warn(String.format("Binding for %s failed, %s can not be assigned to %s.",
                            bindTo.getName(), customClassName, bindTo.getName()));
                }
            } catch (ClassNotFoundException e) {
                logger.error(
                        String.format("Binding for %s failed, class %s specified as property %s can not be found.",
                                bindTo.getName(), customClass, customClassPropName), e);
            }
        } else {
            logger.info(String.format(propertNotFoundErrMsg, arguments));
        }

        return bound;
    }
}
