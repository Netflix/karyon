package com.netflix.karyon.archaius;

import com.netflix.governator.guice.annotations.Bootstrap;

import javax.inject.Provider;
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
@Bootstrap(ArchaiusSuite.class)
public @interface Archaius {

    Class<? extends Provider<PropertiesLoader>> loader() default DefaultPropertiesLoaderProvider.class;
}