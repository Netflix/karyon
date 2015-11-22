package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.util.Modules;
import com.netflix.governator.ElementsEx;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.LifecycleManager;
import com.netflix.karyon.annotations.Profiles;
import com.netflix.karyon.conditional.ConditionalSupportModule;
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
 */
public class Karyon {
    protected Set<String>                 profiles          = new LinkedHashSet<>();
    protected List<ModuleListProvider>    moduleProviders   = new ArrayList<>();
    protected Stage                       stage             = Stage.DEVELOPMENT;
    protected List<Module>                modules           = new ArrayList<>();
    protected List<Module>                overrideModules   = new ArrayList<>();
    protected List<ModuleListTransformer> transformers      = new ArrayList<>();
    protected List<MatchingAutoBinder>    autoBinders       = new ArrayList<>();
    protected IdentityHashMap<KaryonFeature<?>, Object>  features  = new IdentityHashMap<>();
    
    // This is a hack to make sure that if archaius is used at some point we make use
    // of the bridge so any access to the legacy Archaius1 API is actually backed by 
    // the Archaius2 implementation
    static {
        System.setProperty("archaius.default.configuration.class",      "com.netflix.archaius.bridge.StaticAbstractConfiguration");
        System.setProperty("archaius.default.deploymentContext.class",  "com.netflix.archaius.bridge.StaticDeploymentContext");
    }
    
    @Singleton
    class KaryonFeatureConfigImpl implements KaryonFeatureSet {
        private final IdentityHashMap<KaryonFeature<?>, Object> features;
        
        @Inject
        private PropertySource properties;
        
        @Inject
        public KaryonFeatureConfigImpl(IdentityHashMap<KaryonFeature<?>, Object> features) {
            this.features = features;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(KaryonFeature<T> feature) {
            if (features.containsKey(feature)) {
                return (T) features.get(feature);
            }
            return (T) properties.get(feature.getKey(), feature.getType());
        }
    }

    private Karyon() {
        this(null);
    }
    
    @Deprecated
    protected Karyon(String applicationName) {
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
     * Specify the Guice stage in which the application is running.  By default Karyon
     * runs in Stage.DEVELOPMENT to achieve default lazy singleton behavior. 
     * @param stage
     * @return
     */
    public Karyon inStage(Stage stage) {
        this.stage = stage;
        return this;
    }
    
    /**
     * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
     * @param provider
     * @return
     */
    public Karyon addAutoModuleListProvider(ModuleListProvider provider) {
        if (provider != null) {
            this.moduleProviders.add(provider);
        }
        return this;
    }
    
    /**
     * Add a runtime profile
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
     * Add a runtime profiles
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
     * Add a runtime profiles
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
    public Karyon enableFeature(KaryonFeature<Boolean> feature) {
        return setFeature(feature, true);
    }
    
    /**
     * Enable or disable the specified feature
     * @param feature
     */
    public Karyon enableFeature(KaryonFeature<Boolean> feature, boolean enabled) {
        return setFeature(feature, enabled);
    }

    /**
     * Disable the specified feature
     * @param feature
     */
    public Karyon disableFeature(KaryonFeature<Boolean> feature) {
        return setFeature(feature, false);
    }
    
    /**
     * Disable the specified feature
     * @param feature
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
     * @param transformer
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
     * @param matcher    Matcher to restrict the types for which the AutoBinder can be used.  See {@link TypeLiteralMatchers} for
     *                   specifying common matchers.
     * @param autoBinder The auto binder
     */
    public <T extends TypeLiteral<?>> Karyon addAutoBinder(Matcher<T> matcher, AutoBinder autoBinder) {
        this.autoBinders.add(new MatchingAutoBinder<T>(matcher, autoBinder));
        return this;
    }
    
    @Deprecated
    public Karyon apply(AbstractModule ... modules) {
        addModules(modules);
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
     */
    public LifecycleInjector start(String[] args) {
        for (ModuleListTransformer transformer : transformers) {
            modules = transformer.transform(Collections.unmodifiableList(modules));
        }
        
        final Logger LOG = LoggerFactory.getLogger(Karyon.class);
        
        // Create the main LifecycleManager to be used by all levels
        final LifecycleManager manager = new LifecycleManager();
        
        // Construct the injector using our override structure
        try {
            final Module coreModule = Modules.override(
                    ImmutableList.<Module>builder()
                        .addAll(modules)
                        .add(new LifecycleModule())
                        .add(new ConditionalSupportModule())
                        .add(new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(KaryonFeatureSet.class).toInstance(new KaryonFeatureConfigImpl(new IdentityHashMap<>(features)));
                                    bind(LifecycleManager.class).toInstance(manager);
                                    
                                    Multibinder<String> profiles = Multibinder.newSetBinder(binder(), String.class, Profiles.class);
                                    for (String profile : Karyon.this.profiles) {
                                        profiles.addBinding().toInstance(profile);
                                    }
                                }
                            })
                        .build())
                    .with(overrideModules);
            
            for (Element binding : Elements.getElements(coreModule)) {
                LOG.debug("Binding : {}", binding);
            }
            
            Injector injector = Guice.createInjector(
                    stage,
                    coreModule,
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            Set<Key<?>> boundKeys = ElementsEx.getAllBoundKeys(Elements.getElements(coreModule));
                            Set<Key<?>> injectionKeys = ElementsEx.getAllInjectionKeys(Elements.getElements(coreModule));
                            injectionKeys.removeAll(boundKeys);
                            
                            for (Key<?> key : injectionKeys) {
                                for (MatchingAutoBinder factory : Karyon.this.autoBinders) {
                                    if (factory.configure(binder(), key)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    );
            manager.notifyStarted();
            return new LifecycleInjector(injector, manager);
        }
        catch (Throwable e) {
            e.printStackTrace(System.err);
            try {
                manager.notifyStartFailed(e);
            }
            catch (Exception e2) {
                System.err.println("Failed to notify injector creation failure!");
                e2.printStackTrace(System.err);
            }
            return new LifecycleInjector(null, manager);
        }
    }

    public static Karyon newBuilder() {
        return new Karyon();
    }
    
    /**
     * Starting point for creating a Karyon application.
     * 
     * @param applicationName
     * @return
     */
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
    
    @Deprecated
    public static Karyon from(KaryonModule ... modules) {
        Karyon karyon = new Karyon();
        if (modules != null) {
            for (KaryonModule module : modules) {
                karyon.apply(module);
            }
        }
        return karyon;
    }
}
