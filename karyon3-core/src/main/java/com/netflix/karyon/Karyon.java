package com.netflix.karyon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.inject.Module;
import com.netflix.governator.Governator;
import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.LifecycleInjector;

/**
 * Karyon is the core bootstrapper for a Guice based application with auto module loading
 * capabilities based on profiles and module conditionals.
 * 
 * Karyon takes the approach that the application entry point should only be responsible
 * for creating the Guice injector and wait for the application to shut down through various
 * shutdown mechanism.  All application services are specified using Guice modules, with
 * any application services simply being bound asEagerSingleton.  
 * 
<pre>
@{code
@Path("/")
public class HelloWorldApp extends DefaultLifecycleListener {
    public static void main(String[] args) throws InterruptedException {
        Karyon.createInjector(
             new ArchaiusGovernatorConfiguration(),
             new JettyModule(),
             new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    serve("/*").with(GuiceContainer.class);
                    bind(GuiceContainer.class).asEagerSingleton();
                    
                    bind(HelloWorldApp.class).asEagerSingleton();
                }  
            }
            )
            .awaitTermination();
    }
    
    @GET
    public String sayHello() {
        return "hello world";
    }

    @Override
    public void onStarted() {
        System.out.println("Started ***** ");
    }
}
}
</pre>
 * @author elandau
 *
 */
public class Karyon {
    public static LifecycleInjector createInjector(GovernatorConfiguration config) {
        return Governator.createInjector(config, Collections.<Module>emptyList());
    }
    
    public static LifecycleInjector createInjector(GovernatorConfiguration config, Module ... modules) {
        return Governator.createInjector(config, Arrays.asList(modules));
    }
    
    public static LifecycleInjector createInjector(GovernatorConfiguration config, List<Module> modules) {
        return Governator.createInjector(config, modules);
    }
}
