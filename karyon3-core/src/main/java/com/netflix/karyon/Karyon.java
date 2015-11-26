package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.util.Modules;
import com.netflix.karyon.admin.CoreAdminModule;
import com.netflix.karyon.annotations.Arguments;
import com.netflix.karyon.annotations.Priority;
import com.netflix.karyon.annotations.Profiles;
import com.netflix.karyon.api.KaryonFeatureSet;
import com.netflix.karyon.conditional.ConditionalSupportModule;
import com.netflix.karyon.spi.AutoBinder;
import com.netflix.karyon.spi.ModuleListProvider;
import com.netflix.karyon.spi.ModuleListTransformer;

/**
 * Main entry point for creating a LifecycleInjector with guice extensions such as 
 * conditional bindings.  
 * 
 * This injector is constructed in two phases.  The first bootstrap phase determines which core
 * Guice modules should be installed based on processing of conditional annotations.  The final
 * list of auto discovered modules is appended to the main list of modules and installed on the
 * main injector.  
 * 
 * <code>
     Karyon.newBuilder()
        .addModules(
            new ArchaiusKaryonModule(),
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
 */
public class Karyon {
    protected Set<String>                 profiles          = new LinkedHashSet<>();
    protected Stage                       stage             = Stage.DEVELOPMENT;
    protected List<Module>                modules           = new ArrayList<>();
    protected List<Module>                overrideModules   = new ArrayList<>();
    protected List<ModuleListTransformer> transformers      = new ArrayList<>();
    protected Set<AutoBinder>             autoBinders       = new HashSet<>();
    protected IdentityHashMap<KaryonFeature<?>, Object>  features  = new IdentityHashMap<>();
    
    // This is a hack to make sure that if archaius is used at some point we make use
    // of the bridge so any access to the legacy Archaius1 API is actually backed by 
    // the Archaius2 implementation
    static {
        System.setProperty("archaius.default.configuration.class",      "com.netflix.archaius.bridge.StaticAbstractConfiguration");
        System.setProperty("archaius.default.deploymentContext.class",  "com.netflix.archaius.bridge.StaticDeploymentContext");
    }
    
    @Singleton
    class KaryonFeatureSetImpl implements KaryonFeatureSet {
        private final IdentityHashMap<KaryonFeature<?>, Object> features;
        
        @Inject
        private PropertySource properties = DefaultPropertySource.INSTANCE;
        
        @Inject
        public KaryonFeatureSetImpl(IdentityHashMap<KaryonFeature<?>, Object> features) {
            this.features = features;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(KaryonFeature<T> feature) {
            if (features.containsKey(feature)) {
                return (T) features.get(feature);
            }
            else {
                return (T) properties.get(feature.getKey(), feature.getType(), feature.getDefaultValue());
            }
        }
    }

    final static Comparator<AutoBinder> byPriority = new Comparator<AutoBinder>() {
        @Override
        public int compare(AutoBinder o1, AutoBinder o2) {
            int p1 = o1.getClass().isAnnotationPresent(Priority.class) ? o1.getClass().getAnnotation(Priority.class).value() : 0;
            int p2 = o2.getClass().isAnnotationPresent(Priority.class) ? o2.getClass().getAnnotation(Priority.class).value() : 0;
            return p2 - p1;
        }
    };
    
    private Karyon() {
        this(null);
    }
    
    @Deprecated
    protected Karyon(String applicationName) {
    }
    
    /**
     * Add Guice modules to karyon.  
     * 
     * @param modules Guice modules to add.  
     * @return this
     */
    public Karyon addModules(Module ... modules) {
        if (modules != null) {
            this.modules.addAll(Arrays.asList(modules));
        }
        return this;
    }
    
    /**
     * Add Guice modules to karyon.  
     * 
     * @param modules Guice modules to add.  
     * @return this
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
     * @param modules Modules that will be applied as overrides to modules add
     *  or installed via {@link Karyon#addModules(Module...)}
     * @return this
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
     * @param modules Modules that will be applied as overrides to modules add
     *  or installed via {@link Karyon#addModules(Module...)}
     * @return this
     */
    public Karyon addOverrideModules(List<Module> modules) {
        if (modules != null) {
            this.overrideModules.addAll(modules);
        }
        return this;
    }

    /**
     * Specify the Guice stage in which the application is running.  By default Karyon
     * runs in Stage.DEVELOPMENT to achieve default lazy singleton behavior. 
     * @param stage Guice stage
     * @return this
     */
    public Karyon inStage(Stage stage) {
        this.stage = stage;
        return this;
    }
    
    /**
     * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
     * @param provider
     * 
     * @deprecated Module auto loading no longer supported.  Install modules directly and use {@literal @}ProvidesConditionally
     * @return this
     */
    @Deprecated
    public Karyon addAutoModuleListProvider(ModuleListProvider provider) {
        return this;
    }
    
    /**
     * Add a runtime profile.  Profiles are processed by the conditional binding {@literal @}ConditionalOnProfile and
     * are injectable as {@literal @}Profiles Set{@literal <}String{@literal >}.
     * 
     * @param profile A profile
     * @return this
     */
    public Karyon addProfile(String profile) {
        if (profile != null) {
            this.profiles.add(profile);
        }
        return this;
    }

    /**
     * Add a runtime profiles.  Profiles are processed by the conditional binding {@literal @}ConditionalOnProfile and
     * are injectable as {@literal @}Profiles Set{@literal <}String{@literal >}.
     * 
     * @param profiles Set of profiles
     * @return this
     */
    public Karyon addProfiles(String profile, String... profiles) {
        this.profiles.add(profile);
        if (profiles != null) {
            this.profiles.addAll(Arrays.asList(profiles));
        }
        return this;
    }
    
    /**
     * Add a runtime profiles.  Profiles are processed by the conditional binding {@literal @}ConditionalOnProfile and
     * are injectable as {@literal @}Profiles Set{@literal <}String{@literal >}.
     * 
     * @param profiles Set of profiles
     * @return this
     */
    public Karyon addProfiles(Collection<String> profiles) {
        if (profiles != null) {
            this.profiles.addAll(profiles);
        }
        return this;
    }
    
    /**
     * Enable the specified feature
     * @param feature Boolean feature to enable
     * @return this
     */
    public Karyon enableFeature(KaryonFeature<Boolean> feature) {
        return setFeature(feature, true);
    }
    
    /**
     * Enable or disable the specified feature
     * @param feature Boolean feature to disable
     * @return this
     */
    public Karyon enableFeature(KaryonFeature<Boolean> feature, boolean enabled) {
        return setFeature(feature, enabled);
    }

    /**
     * Disable the specified feature
     * @param feature Boolean feature to enable/disable
     * @return this
     */
    public Karyon disableFeature(KaryonFeature<Boolean> feature) {
        return setFeature(feature, false);
    }
    
    /**
     * Set a feature
     * @param feature Feature to set
     * @return this
     */
    public <T> Karyon setFeature(KaryonFeature<T> feature, T value) {
        this.features.put(feature, value);
        return this;
    }
    
    /**
     * Add a ModuleListTransformer that will be invoked on the final list of modules
     * prior to creating the injectors.  Multiple transformers may be added with each
     * transforming the result of the previous one.
     * 
     * @param transformer A transformer
     * @return this
     */
    public Karyon addModuleListTransformer(ModuleListTransformer transformer) {
        if (transformer != null) {
            this.transformers.add(transformer);
        }
        return this;
    }
    
    /**
     * Add an AutoBinder that will be called for any missing bindings.
     * 
     * @param autoBinder The auto binder
     * @return this
     */
    public Karyon addAutoBinder(AutoBinder autoBinder) {
        this.autoBinders.add(autoBinder);
        return this;
    }
    
    @Deprecated
    public Karyon apply(Module ... modules) {
        addModules(modules);
        return this;
    }
    
    /**
     * Create the injector and call any LifecycleListeners
     * @return the LifecycleInjector for this run
     */
    public LifecycleInjector start() {
        return start(null);
    }
    
    /**
     * Create the injector and call any LifecycleListeners
     * @param args - Runtime parameter (from main) injectable as {@literal @}Arguments String[]
     * @return the LifecycleInjector for this run
     */
    public LifecycleInjector start(final String[] args) {
        final Logger LOG = LoggerFactory.getLogger(Karyon.class);
        
        final KaryonFeatureSetImpl featureSet = new KaryonFeatureSetImpl(new IdentityHashMap<>(features));
        
        for (ModuleListTransformer transformer : transformers) {
            modules = transformer.transform(Collections.unmodifiableList(modules));
        }
        
        if (featureSet.get(KaryonFeatures.DISCOVER_AUTO_BINDERS)) {
            for (AutoBinder autoBinder : ServiceLoader.load(AutoBinder.class)) {
                LOG.info("Adding AutoBinder {}", autoBinder.getClass().getName());
                this.addAutoBinder(autoBinder);
            }
        }
        
        // Create the main LifecycleManager to be used by all levels
        final LifecycleManager manager = new LifecycleManager();
        
        // Construct the injector using our override structure
        try {
            final List<Module> coreModules = new ArrayList<>();
            coreModules.addAll(modules);
            coreModules.add(new CoreModule());
            coreModules.add(new CoreAdminModule());
            coreModules.add(new LifecycleModule());
            coreModules.add(new ConditionalSupportModule());
            coreModules.add(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(KaryonFeatureSet.class).toInstance(featureSet);
                    bind(LifecycleManager.class).toInstance(manager);
                    bind(new TypeLiteral<Set<String>>() {}).annotatedWith(Profiles.class).toInstance(profiles);
                    bind(String[].class).annotatedWith(Arguments.class).toInstance(args != null ? args : new String[]{});
                }
            });
            
            // Loop through all elements that have been bound and look for any missing bindings.  
            // For missing bindings try to call the registered AutoBinders.  
            // Since an AutoBinder may add additional bindings the processes is repeated until 
            // no new auto bindings have been created
            //
            // TODO: This is probably not the most efficient way of doing this.  This code could be 
            //       optimized by storing state and only evaluating newly added modules for bindings.
            List<AutoBinder> prioritizedAutoBinders = autoBinders.stream().sorted(byPriority).collect(Collectors.toList());
            boolean done = false;
            while (!done) {
                done = true;
                for (Key<?> key : ElementsEx.getAllUnboundKeys(Elements.getElements(coreModules))) {
                    for (AutoBinder factory : prioritizedAutoBinders) {
                        if (factory.matches(key.getTypeLiteral())) {
                            Module module = factory.getModuleForKey(key);
                            if (module != null) {
                                coreModules.add(module);
                                done = false;
                                break;
                            }
                        }
                    }
                }
            }

            for (Element binding : Elements.getElements(coreModules)) {
                LOG.debug("Binding : {}", binding);
            }
            
            Injector injector = Guice.createInjector(
                stage,
                Modules.override(coreModules).with(overrideModules)
            );
            manager.notifyStarted();
            return new LifecycleInjector(injector, manager);
        }
        catch (ProvisionException|CreationException|ConfigurationException e) {
            LOG.error("Failed to create injector", e);
            try {
                manager.notifyStartFailed(e);
            }
            catch (Exception e2) {
                LOG.error("Failed to notify injector creation failure", e2 );
            }
            if (!featureSet.get(KaryonFeatures.SHUTDOWN_ON_ERROR)) {
                return new LifecycleInjector(null, manager);
            }
            else {
                throw e;
            }
        }
    }

    /**
     * Construct a new Karyon instance
     * @return Karyon instance
     */
    public static Karyon newBuilder() {
        return new Karyon();
    }
    
    public static Karyon forClass(Class<?> applicationMainClass) {
        return newBuilder().addModules(new AbstractModule() {
            @Override
            protected void configure() {
                bind(applicationMainClass).asEagerSingleton();
            }
        });
    }
    
    @Deprecated
    public static Karyon forApplication(String applicationName) {
        return new Karyon(applicationName);
    }
    
    @Deprecated
    public static Karyon create() {
        return new Karyon();
    }
    
    @Deprecated
    public static Karyon create(Module ... modules) {
        return new Karyon().addModules(modules);
    }
}
