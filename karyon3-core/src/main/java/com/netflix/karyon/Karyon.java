package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.api.KaryonFeature;
import com.netflix.karyon.api.KaryonFeatures;
import com.netflix.karyon.api.PropertySource;
import com.netflix.karyon.conditional.ConditionalOnProfile;
import com.netflix.karyon.spi.ModuleListTransformer;

/**
 * Base entry point for creating a LifecycleInjector with module auto-loading capabilities.  
 * Module auto-loading makes it possible to load bindings that are contextual to the 
 * environment in which the application is running based on things like profiles,
 * properties and existing bindings.  
 * 
 * The LifecycleInjector created here uses a layered approach to construct the Guice Injector
 * so that bindings can be overridden at a high level based on the runtime environment
 * as opposed to sprinkling Modules.overrides and conditionals throughout the modules themselves
 * since using Modules.overrides directly looses the original module's context and can easily result
 * in difficult to debug duplicate binding errors.
 * 
 * This injector is constructed in two phases.  The first bootstrap phase determines which core
 * Guice modules should be installed based on processing of conditional annotations.  The final
 * list of auto discovered modules is appended to the main list of modules and installed on the
 * main injector.  Application level override modules may be applied to this final list from the
 * list of modules returned from {@link KaryonConfiguration#getOverrideModules()}.
 * 
 * <code>
     Karyon.forApplication("foo")
        .addModules(
             new JettyModule(),
             new JerseyServletModule() {
                {@literal @}@Override
                protected void configureServlets() {
                    serve("/*").with(GuiceContainer.class);
                    bind(GuiceContainer.class).asEagerSingleton();
                    
                    bind(HelloWorldApp.class).asEagerSingleton();
                }  
            }
        )
        .start()
        .awaitTermination();
 * </code>
 * 
 *      +-------------------+
 *      |      Override     |
 *      +-------------------+
 *      |   Auto Override   |
 *      +-------------------+
 *      |    Core + Auto    |
 *      +-------------------+
 *      | Bootstrap Exposed |
 *      +-------------------+
 *      |  Default Modules  |
 *      +-------------------+
 */
public class Karyon {
    private static final String KARYON_PROFILES = "karyon.profiles";
    
    private static final String DEFAULT_APPLICATION_NAME = "KaryonApplication";
    
    protected final String                applicationName;
    protected PropertySource              propertySource    = DefaultPropertySource.INSTANCE;
    protected Set<String>                 profiles          = new LinkedHashSet<>();
    protected List<ModuleListProvider>    moduleProviders   = new ArrayList<>();
    protected Map<KaryonFeature, Boolean> features          = new HashMap<>();
    protected Stage                       stage             = Stage.DEVELOPMENT;
    protected List<Module>                defaultModules    = new ArrayList<>();
    protected List<Module>                modules           = new ArrayList<>();
    protected List<Module>                overrideModules   = new ArrayList<>();
    protected List<ModuleListTransformer> transformers      = new ArrayList<>();

    // This is a hack to make sure that if archaius is used at some point we make use
    // of the bridge so any access to the legacy Archaius1 API is actually backed by 
    // the Archaius2 implementation
    static {
        System.setProperty("archaius.default.configuration.class",      "com.netflix.archaius.bridge.StaticAbstractConfiguration");
        System.setProperty("archaius.default.deploymentContext.class",  "com.netflix.archaius.bridge.StaticDeploymentContext");
    }
    
    protected Karyon(String applicationName) {
        this.applicationName = applicationName;
    }
    
    /**
     * Add main Guice modules to your application
     * @param modules
     * @return
     */
    public Karyon addModules(Module ... modules) {
        if (modules != null) {
            this.modules.addAll(Arrays.asList(modules));
        }
        return this;
    }
    
    /**
     * Add main Guice modules to your application
     * @param modules
     * @return
     */
    public Karyon addModules(List<Module> modules) {
        if (modules != null) {
            this.modules.addAll(modules);
        }
        return this;
    }
    
    /**
     * Add override modules for any modules add via addModules or that are 
     * conditionally loaded.  This is useful for testing or when an application
     * absolutely needs to override a binding to fix a binding problem in the
     * code modules
     * @param modules
     * @return
     */
    public Karyon addOverrideModules(Module ... modules) {
        if (modules != null) {
            this.overrideModules.addAll(Arrays.asList(modules));
        }
        return this;
    }
    
    /**
     * Add override modules for any modules add via addModules or that are 
     * conditionally loaded.  This is useful for testing or when an application
     * absolutely needs to override a binding to fix a binding problem in the
     * code modules
     * @param modules
     * @return
     */
    public Karyon addOverrideModules(List<Module> modules) {
        if (modules != null) {
            this.overrideModules.addAll(modules);
        }
        return this;
    }
    
    /**
     * Add a module with default bindings that may be overridden by the core 
     * list of modules.  This is an alternative to using Guice's ImplementedBy
     * defaults as it makes it couples the interface definition with the default
     * implementation and all of its dependencies.
     * 
     * @param modules
     */
    public Karyon addDefaultModules(List<Module> modules) {
        if (modules != null) {
            this.defaultModules.addAll(modules);
        }
        return this;
    }
    
    /**
     * Add a module with default bindings that may be overridden by the core 
     * list of modules.  This is an alternative to using Guice's ImplementedBy
     * defaults as it makes it couples the interface definition with the default
     * implementation and all of its dependencies.
     * 
     * @param modules
     */
    public Karyon addDefaultModules(Module ... modules) {
        if (modules != null) {
            this.defaultModules.addAll(Arrays.asList(modules));
        }
        return this;
    }

    /**
     * Specify the Guice stage in which the application is running.  By default Karyon
     * runs in Stage.DEVELOPMENT to achieve default lazy singleton behavior. 
     * @param stage
     */
    public Karyon inStage(Stage stage) {
        this.stage = stage;
        return this;
    }
    
    /**
     * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
     * @param provider
     */
    public Karyon addAutoModuleListProvider(ModuleListProvider provider) {
        if (provider != null) {
            this.moduleProviders.add(provider);
        }
        return this;
    }
    
    /**
     * Add a runtime profile.  @see {@link ConditionalOnProfile}
     * 
     * @param profile
     */
    public Karyon addProfile(String profile) {
        if (profile != null) {
            this.profiles.add(profile);
        }
        return this;
    }

    /**
     * Add a runtime profiles.  @see {@link ConditionalOnProfile}
     * 
     * @param profiles
     */
    public Karyon addProfiles(String... profiles) {
        if (profiles != null) {
            this.profiles.addAll(Arrays.asList(profiles));
        }
        return this;
    }
    
    /**
     * Add a runtime profiles.  @see {@link ConditionalOnProfile}
     * 
     * @param profiles
     */
    public Karyon addProfiles(Collection<String> profiles) {
        if (profiles != null) {
            this.profiles.addAll(profiles);
        }
        return this;
    }
    
    /**
     * Enable the specified feature
     * @param feature
     */
    public Karyon enableFeature(KaryonFeature feature) {
        return enableFeature(feature, true);
    }
    
    /**
     * Enable or disable the specified feature
     * @param feature
     */
    public Karyon enableFeature(KaryonFeature feature, boolean enabled) {
        if (feature != null) {
            this.features.put(feature, enabled);
        }
        return this;
    }

    /**
     * Disable the specified feature
     * @param feature
     */
    public Karyon disableFeature(KaryonFeature feature) {
        return enableFeature(feature, false);
    }
    
    public Karyon setPropertySource(PropertySource propertySource) {
        this.propertySource = propertySource;
        return this;
    }

    public PropertySource getPropertySource() {
        return this.propertySource;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    /**
     * Add a ModuleListTransformer that will be invoked on the final list of modules
     * prior to creating the injectors.  Multiple transformers may be added with each
     * transforming the result of the previous one.
     * 
     * @param transformer
     */
    public Karyon addModuleListTransformer(ModuleListTransformer transformer) {
        if (transformer != null) {
            this.transformers.add(transformer);
        }
        return this;
    }
    
    /**
     * Call this anywhere in the process of manipulating the builder to apply a reusable
     * sequence of calls to the builder 
     * 
     * @param modules
     */
    public Karyon apply(KaryonModule ... modules) {
        if (modules != null) {
            for (KaryonModule module : modules) {   
                module.configure(this);
            }
        }
        return this;
    }
    
    /**
     * 
     */
    public LifecycleInjector start() {
        return start(null);
    }
    
    /**
     * Shortcut to creating the injector
     * @return The builder
     */
    public LifecycleInjector start(String[] args) {
        this.addDefaultModules(new KaryonDefaultsModule());
        
        if (this.getPropertySource().equals(DefaultPropertySource.INSTANCE) && isFeatureEnabled(KaryonFeatures.USE_ARCHAIUS)) { 
            try {
                apply((KaryonModule) Class.forName("com.netflix.karyon.archaius.ArchaiusKaryonModule").newInstance());
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to bootstrap using archaius. Either add a dependency on 'com.netflix.karyon:karyon3-archaius2' or disable the feature KaryonFeatures.USE_ARCHAIUS");
            } 
            catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to bootstrap using archaius");
            }
        }
        
        String karyonProfiles = getPropertySource().get(KARYON_PROFILES);
        if (karyonProfiles != null) {
             addProfiles(karyonProfiles);
        }
        
        if (isFeatureEnabled(KaryonFeatures.USE_DEFAULT_KARYON_MODULE)) 
            apply(new DefaultKaryonModule());
        
        for (ModuleListTransformer transformer : transformers) {
            modules = transformer.transform(Collections.unmodifiableList(modules));
        }
        
        return LifecycleInjectorCreator.createInjector(new KaryonConfiguration() {
            @Override
            public List<Module> getModules() {
                return modules;
            }

            @Override
            public List<Module> getOverrideModules() {
                return overrideModules;
            }

            @Override
            public List<ModuleListProvider> getAutoModuleListProviders() {
                return moduleProviders;
            }

            @Override
            public List<Module> getDefaultModules() {
                return defaultModules;
            }
            
            @Override
            public Set<String> getProfiles() {
                return profiles;
            }

            @Override
            public PropertySource getPropertySource() {
                return propertySource;
            }

            @Override
            public Stage getStage() {
                return stage;
            }

            @Override
            public boolean isFeatureEnabled(KaryonFeature feature) {
                return Karyon.this.isFeatureEnabled(feature);
            }
        });
    }

    private boolean isFeatureEnabled(KaryonFeature feature) {
        if (propertySource != null) {
            Boolean value = propertySource.get(feature.getKey(), Boolean.class);
            if (value != null && value == true) {
                return true;
            }
        }
        Boolean value = features.get(feature);
        return value == null
                ? feature.isEnabledByDefault()
                : value;
    }
    
    /**
     * Starting point for creating a Karyon application.
     * 
     * @param applicationName
     * @return
     */
    public static Karyon forApplication(String applicationName) {
        return new Karyon(applicationName);
    }
    
    /**
     * @deprecated Call Karyon.forApplication("foo")
     */
    @Deprecated
    public static Karyon create() {
        return new Karyon(DEFAULT_APPLICATION_NAME);
    }
    
    /**
     * @deprecated Call Karyon.forApplication("foo").addModules(modules)
     */
    @Deprecated
    public static Karyon create(Module ... modules) {
        return new Karyon(DEFAULT_APPLICATION_NAME).addModules(modules);
    }
    
    /**
     * @deprecated Call Karyon.forApplication("foo").apply(modules)
     */
    @Deprecated
    public static Karyon from(KaryonModule ... modules) {
        Karyon karyon = new Karyon(DEFAULT_APPLICATION_NAME);
        if (modules != null) {
            for (KaryonModule module : modules) {
                karyon.apply(module);
            }
        }
        return karyon;
    }
}
