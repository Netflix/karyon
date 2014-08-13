package com.netflix.karyon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Module;
import com.netflix.governator.annotations.Modules;
import com.netflix.governator.guice.annotations.Bootstrap;
import com.netflix.governator.guice.bootstrap.ModulesBootstrap;

/**
 * All Karyon server modules shall be enlisted inside {@link Submodules} annotation.
 * Each submodule initializes own child injector, to allow instantiation of
 * multiple instances of the same server type (HTTP, TCP, etc).
 *
 * Common services shall be provided with {@link Modules} annotation. They
 * will be instantiated in the root injector.
 *
 * @author Tomasz Bak
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Bootstrap(ModulesBootstrap.class)
public @interface Submodules {

    Class<? extends Module>[] include() default {};

}
