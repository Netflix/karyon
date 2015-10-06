package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.conditional.ConditionalOnProfile;

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
 * list of modules returned from {@link KaryonConfiguration.getOverrideModules()}.
 * 
 * <pre>
 * {@code
     Karyon
        .bootstrap()
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
        .start()
        .awaitTermination();
 * }
 * </pre>
 * 
 * @param config
 * @param modules
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
 *      
 * @see ArchaiusKaryon
 * 
 * @author elandau
 *
 * @param <T>
 */
public class Karyon<T extends Karyon<T>> {
    private static final String KARYON_PROFILES = "karyon.profiles";
    
    public static abstract class Dsl<T extends Dsl<T>> implements KaryonDsl<T> {
        protected PropertySource              propertySource    = new DefaultPropertySource();
        protected Set<String>                 profiles          = new LinkedHashSet<>();
        protected List<ModuleListProvider>    moduleProviders   = new ArrayList<>();
        protected Map<KaryonFeature, Boolean> features          = new HashMap<>();
        protected Stage                       stage             = Stage.DEVELOPMENT;
        protected List<Module>                modules           = new ArrayList<>();
        protected List<Module>                overrideModules   = new ArrayList<>();

        /**
         * Add main Guice modules to your application
         * @param modules
         * @return
         */
        public T addModules(Module ... modules) {
            this.modules.addAll(Arrays.asList(modules));
            return self();
        }
        
        /**
         * Add main Guice modules to your application
         * @param modules
         * @return
         */
        public T addModules(List<Module> modules) {
            modules.addAll(modules);
            return self();
        }
        
        /**
         * Add override modules for any modules add via addModules or that are 
         * conditionally loaded.  This is useful for testing or when an application
         * absolutely needs to override a binding to fix a binding problem in the
         * code modules
         * @param modules
         * @return
         */
        public T addOverrideModules(Module ... modules) {
            overrideModules.addAll(Arrays.asList(modules));
            return self();
        }
        
        /**
         * Add override modules for any modules add via addModules or that are 
         * conditionally loaded.  This is useful for testing or when an application
         * absolutely needs to override a binding to fix a binding problem in the
         * code modules
         * @param modules
         * @return
         */
        public T addOverrideModules(List<Module> modules) {
            overrideModules.addAll(modules);
            return self();
        }

        /**
         * Specify the Guice stage in which the application is running.  By default Karyon
         * runs in Stage.DEVELOPMENT to achieve default lazy singleton behavior. 
         * @param stage
         * @return
         */
        public T inStage(Stage stage) {
            this.stage = stage;
            return self();
        }
        
        /**
         * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
         * @param finder
         * @return
         */
        public T addAutoModuleListProvider(ModuleListProvider finder) {
            moduleProviders.add(finder);
            return self();
        }
        
        /**
         * Add a runtime profile.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfile(String profile) {
            if (profile != null) {
                profiles.add(profile);
            }
            return self();
        }

        /**
         * Add a runtime profiles.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfiles(String... profiles) {
            if (profiles != null) {
                this.profiles.addAll(Arrays.asList(profiles));
            }
            return self();
        }
        
        /**
         * Add a runtime profiles.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfiles(Collection<String> profiles) {
            if (profiles != null) {
                this.profiles.addAll(profiles);
            }
            return self();
        }
        
        /**
         * Enable the specified feature
         * @param feature
         */
        public T enableFeature(KaryonFeature feature) {
            if (feature != null) {
                features.put(feature, true);
            }
            return self();
        }

        /**
         * Disable the specified feature
         * @param feature
         */
        public T disableFeature(KaryonFeature feature) {
            if (feature != null) {
                features.put(feature, false);
            }
            return self();
        }
        
        public T setPropertySource(PropertySource propertySource) {
            this.propertySource = propertySource;
            return self();
        }

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
        public T using(KaryonDslModule suite) throws Exception {
            suite.configure(this);
            return self();
        }
        
        /**
         * Shortcut to creating the injector
         * @return The builder
         * @throws Exception
         */
        public LifecycleInjector start() throws Exception {
            preCreate();
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
                    return Dsl.this.isFeatureEnabled(feature);
                }
            });
        }
        
        protected void preCreate() throws Exception {
            String karyonProfiles = getPropertySource().get(KARYON_PROFILES);
            if (karyonProfiles != null) {
                 addProfiles(karyonProfiles);
            }
            
            if (isFeatureEnabled(KaryonFeatures.USE_DEFAULT_PACKAGES)) 
                using(new DefaultKaryonSuite());
        }
        
        boolean isFeatureEnabled(KaryonFeature feature) {
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
        
        abstract protected T self();
    }
    
    private static class BuilderWrapper extends Dsl<BuilderWrapper> {
        @Override
        protected BuilderWrapper self() {
            return this;
        }
    }

    public static Dsl<?> bootstrap() {
        return new BuilderWrapper();
    }
}
