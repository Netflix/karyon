package com.netflix.karyon;

import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.auto.ModuleListProviders;
import com.netflix.governator.auto.PropertySource;

/**
 * Extension to Governator's {@link DefaultGovernatorConfiguration.Builder} for specific
 * use in Karyon with the following added functionality,
 * 
 * 1.  Capture a sequence of calls to the Builder using zero or more {@link KaryonSuite}'s
 * 2.  start() method to create the actual injector as a shortcut to calling
 *          Goverantor.createInjector(builder.build())
 * 3.  startAndAwaitTermination() to create and block on the application lifecycle as a shortcut
 *     to calling
 *          Goverantor.createInjector(builder.build()).startAndAwaitTermination()
 * 
 * @author elandau
 *
 */
public class KaryonBuilder extends DefaultGovernatorConfiguration.Builder<KaryonBuilder> {
    private static final String KARYON_PROFILES = "karyon.profiles";

    public PropertySource getPropertySource() {
        return this.propertySource;
    }
    
    /**
     * Call this anywhere in the process of manipulating the builder to apply a reusable
     * sequence of calls to the builder 
     * 
     * @param suite
     * @return The builder
     * @throws Exception
     */
    public KaryonBuilder using(KaryonSuite suite) throws Exception {
        suite.configure(this);
        return this;
    }
    
    /**
     * Shortcut to creating the injector
     * @return The builder
     * @throws Exception
     */
    public LifecycleInjector start() throws Exception {
        return Governator.createInjector(build());
    }
    
    /**
     * Shortcut to creating the injector and waiting for it to terminate
     * @throws Exception
     */
    public void startAndAwaitTermination() throws Exception {
        start().awaitTermination();
    }
    
    @Override
    protected void initialize() throws Exception {
        super.initialize();
        
        addModule(new CoreModule());
        
        // TODO: This should probably not be added for ALL instances of KaryonBuilder.
        addModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"));
         
        String karyonProfiles = getPropertySource().get(KARYON_PROFILES);
        if (karyonProfiles != null) {
             addProfiles(karyonProfiles);
        }
    }
    
    @Override
    protected KaryonBuilder This() {
        return this;
    }
}
