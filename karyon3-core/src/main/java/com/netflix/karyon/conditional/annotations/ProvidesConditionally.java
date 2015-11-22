package com.netflix.karyon.conditional.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditional binding makes it possible to provide duplicate bindings for a type 
 * with each binding being conditional on things like profiles, properties, etc. 
 * 
 * To enable, install ConditionalSupportModule in Guice to enable conditional binding.
 * Note that {@link Karyon} auto-installs this module.
 * <code>
 * install(new ConditionalSupportModule())
 * </code>
 * 
 * <code>
 * public static class MyModule extends AbstractModule {
 *     @Override
 *     protected void configure() {
 *     }
 *     
 *     @ProvidesConditionally(isDefault=true)
 *     @ConditionalOnProfile("test")
 *     public Foo getFooForTest() {
 *         return new FooForTest();
 *     }
 *     
 *     @ProvidesConditionally
 *     @ConditionalOnProfile("prod") 
 *     public Foo getFooForProd() {
 *         return new FooForProd();
 *     }
 * }
 * </code>
 * 
 * Foo can now be injected directly with the 
 * 
 * <code>
 * @Inject
 * public Bar(Foo foo) {
 * }
 * </code>
 * 
 * Note that a ProvisionException will be thrown unless exactly one of the conditions is met.
 * If no conditions are met the ProvidesConditionally with isDefault set to true will be used,
 * otherwise a missing bindings exception will be thrown.  Only one ProvidesConditionally may
 * have isDefault set to true. 
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProvidesConditionally {
    /**
     * If set to true this bindings will be used if not other condition is met (even if this bindings
     * condition is not met).  Only one ProvidesConditionally for a type may hav isDefault set to true
     * @return
     */
    boolean isDefault() default false;
}
