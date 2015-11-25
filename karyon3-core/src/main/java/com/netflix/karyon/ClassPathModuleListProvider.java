package com.netflix.karyon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.reflect.ClassPath;
import com.google.inject.Module;
import com.netflix.karyon.spi.ModuleListProvider;

/**
 * ClassPath scanner using Guava's ClassPath
 * 
 * @author elandau
 */
final class ClassPathModuleListProvider implements ModuleListProvider {

    private List<String> packages;

    public ClassPathModuleListProvider(String... packages) {
        this.packages = Arrays.asList(packages);
    }
    
    public ClassPathModuleListProvider(List<String> packages) {
        this.packages = packages;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Module> get() {
        List<Module> modules = new ArrayList<>();
        ClassPath classpath;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String pkg : packages) {
            try {
                classpath = ClassPath.from(loader);
                for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(pkg)) {
                    try {
                        // Include Modules that have at least on Conditional
                        Class<?> cls = Class.forName(classInfo.getName(), false, loader);
                        if (   Modifier.isPublic(cls.getModifiers())
                            && !cls.isInterface() 
                            && !Modifier.isAbstract( cls.getModifiers() ) 
                            && Module.class.isAssignableFrom(cls)) {
                            if (isAllowed((Class<? extends Module>) cls)) {
                                modules.add((Module) cls.newInstance());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate module '" + classInfo.getName() + "'", e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to scan root package '" + pkg + "'", e);
            }
        }
        return modules;
    }
    
    protected boolean isAllowed(Class<? extends Module> module) {
        return true;
    }
}
