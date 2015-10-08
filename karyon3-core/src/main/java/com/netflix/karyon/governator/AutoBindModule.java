package com.netflix.karyon.governator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.netflix.governator.annotations.AutoBindSingleton;
import com.netflix.karyon.AbstractKaryonModule;
import com.netflix.karyon.KaryonModule;

/**
 * Backwards compatibility support for governator's AutoBindSingleton annotation. 
 * 
 * To enable,
 * 
 * <code>
 * Karyon.create()
 *       .apply(new AutoBindModule()
 *          .includePackages("com.example")
 *       )
 *       .start();
 * </code>
 * 
 * Note that classpath scanning can be dangerous as it may pick up unwanted classes.  It is possible
 * to ignore specific classes and packages from the classpath scanning result.  Ignore always takes
 * Precedence over any classes under the included packages.  Package ignore is done using a prefix
 * match so all subpackages will also be ignored.
 * 
 * <code>
 * Karyon.create()
 *       .apply(new AutoBindModule()
 *          .includePackages("com.example")
 *          .ignoreClasses("com.example.ignore.SomeClassToIgnore")
 *          .ignorePackages("com.example.ignore")
 *       )
 *       .start();
 * </code>
 * @author elandau
 *
 */
public class AutoBindModule extends AbstractKaryonModule {
    private List<String> packages = Lists.newArrayList();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private Set<String> classesToIgnore = new HashSet<>();
    private Set<String> packagesToIgnore = new HashSet<>();
    
    /**
     * Specify packages to include in classpath scanning
     * @param packages
     */
    public AutoBindModule includePackages(String... packages) {
        if (packages != null) {
            this.packages.addAll(Arrays.asList(packages));
        }
        return this;
    }
    
    /**
     * Specify the class loader to use.  By default, Thread.currentThread().getContextClassLoader()
     * will be used.
     * 
     * @param loader
     */
    public AutoBindModule withClassLoader(ClassLoader loader) {
        this.classLoader = loader;
        return this;
    }
    
    /**
     * Specify class names to ignore.  
     * @param classes
     */
    public KaryonModule ignoreClassNames(String ... classes) {
        if (classes != null) {
            return ignoreClassNames(Arrays.asList(classes));
        }
        return this;
    }
    
    /**
     * Specify class names to ignore
     * @param classes
     */
    public KaryonModule ignoreClassNames(Collection<String> classes) {
        if (classes != null) {
            for (String cls : classes) {
                classesToIgnore.add(cls);
            }
        }
        return this;
    }

    /**
     * Specify class types to ignore
     * @param classes
     */
    public KaryonModule ignoreClasses(Class<?> ... classes) {
        if (classes != null) {
            return ignoreClasses(Arrays.asList(classes));
        }
        return this;
    }
    
    /**
     * Specify class types to ignore
     * @param classes
     */
    public KaryonModule ignoreClasses(Collection<Class<?>> classes) {
        if (classes != null) {
            for (Class<?> cls : classes) {
                classesToIgnore.add(cls.getName());
            }
        }
        return this;
    }

    /**
     * Specify packages to ignore.  Note that all sub-packages will be ignored as well.
     * @param packages
     */
    public KaryonModule ignorePackages(String ... packages) {
        if (packages != null) {
            return ignorePackages(Arrays.asList(packages));
        }
        return this;
    }
    
    /**
     * Specify packages to ignore.  Note that all sub-packages will be ignored as well.
     * @param packages
     */
    public KaryonModule ignorePackages(Collection<String> packages) {
        if (packages != null) {
            for (String pkg : packages) {
                if (pkg.endsWith(".")) 
                    packagesToIgnore.add(pkg);
                else 
                    packagesToIgnore.add(pkg + ".");
            }
        }
        return this;
    }

    private boolean shouldIncludeClass(ClassPath.ClassInfo info) {
        // Include Modules that have at least on Conditional
        if (classesToIgnore.contains(info.getName())) {
            return false;
        }
        for (String packageToIgnore : packagesToIgnore) {
            if (info.getName().startsWith(packageToIgnore)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void configure() {
        ClassPath classpath;
        final List<Module> modules = new ArrayList<>();
        final List<Class<?>> singletons = new ArrayList<>();
        for (String pkg : packages) {
            try {
                classpath = ClassPath.from(classLoader);
                for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(pkg)) {
                    try {
                        Class<?> cls = Class.forName(classInfo.getName(), false, classLoader);
                        AutoBindSingleton abs = cls.getAnnotation(AutoBindSingleton.class);
                        if (abs != null & shouldIncludeClass(classInfo)) {
                            if (Module.class.isAssignableFrom(cls)) {
                                modules.add((Module) cls.newInstance());
                            }
                            else {
                                singletons.add(cls);
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate module '" + classInfo.getName() + "'", e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to scan root package '" + pkg + "'", e);
            }
        }
        
        addModules(new AbstractModule() {
            @Override
            protected void configure() {
                install(Modules.combine(modules));
                for (Class<?> singleton : singletons) {
                    bind(singleton).asEagerSingleton();
                }
            }
            
            @Override
            public String toString() {
                return "AutoBindSingletonModule[]";
            }
        });
    }
}
