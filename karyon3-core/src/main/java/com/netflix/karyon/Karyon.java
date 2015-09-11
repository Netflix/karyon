package com.netflix.karyon;

import com.netflix.governator.Governator;
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
             ArchaiusGovernatorConfiguration.builder()
                 .addModules(
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
                .build()
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
    public static LifecycleInjector createInjector(KaryonConfiguration config) {
        return Governator.createInjector(config);
    }
}