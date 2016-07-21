package netflix.karyon;

import com.netflix.governator.guice.annotations.Bootstrap;
import netflix.karyon.health.AlwaysHealthyHealthCheck;
import netflix.karyon.health.HealthCheckHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Bootstrap(bootstrap = KaryonBootstrapModule.class)
@Deprecated
public @interface KaryonBootstrap {

    String name();

    Class<? extends HealthCheckHandler> healthcheck() default AlwaysHealthyHealthCheck.class;
}
