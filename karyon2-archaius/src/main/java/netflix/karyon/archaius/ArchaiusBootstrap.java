package netflix.karyon.archaius;

import com.netflix.governator.guice.annotations.Bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Nitesh Kant
 */
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Bootstrap(bootstrap = ArchaiusBootstrapModule.class)
public @interface ArchaiusBootstrap {

    Class<? extends PropertiesLoader> loader() default DefaultPropertiesLoader.class;
}