package com.netflix.karyon.archaius;

import com.netflix.governator.guice.annotations.Bootstrap;

import javax.inject.Provider;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Governator bootstrap annotation to enable property loading via archaius
 * 
 * Example,
 * <pre>
 * {@code 
 * @KaryonBootstrap(name="foo")
 * @ArchaiusBootstrap
 * public class MyApplication {
 * }
 * }
 * </pre>
 * 
 * The above example will initialize Archaius with properties using cascading property
 * loading based on the KaryonBootstrap application name 'foo'.
 * 
 * @author Nitesh Kant
 * @author elandau
 */
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Bootstrap(ArchaiusSuite.class)
public @interface ArchaiusBootstrap {

    @Deprecated 
    Class<? extends Provider<PropertiesLoader>> loader() default DefaultPropertiesLoaderProvider.class;
    
    /**
     * Enable including/excluding of modules from properties.  Modules may be specified as follows
     * 
     * karyon.modules.com.example.TestModule=include
     * karyon.modules.com.example.TestModule=exclude
     * 
     * where 'karyon.modules' may be overridden by changing prefix()
     * 
     */
    boolean enableModuleLoading() default true;
    
    /**
     * Prefix to use when loading modules from properties (enableModuleLoading=true)
     * @return
     */
    String prefix() default "karyon.modules";
    
    /**
     * List of {@link PropertiesLoader}'s that are loaded after Archaius is initialized but before dynamic
     * modules or any other operation is performed
     * @return
     */
    Class<? extends PropertiesLoader>[] overrides() default {};
}