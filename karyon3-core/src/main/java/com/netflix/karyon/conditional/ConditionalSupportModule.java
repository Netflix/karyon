package com.netflix.karyon.conditional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import com.netflix.karyon.conditional.annotations.Conditional;
import com.netflix.karyon.conditional.annotations.ConditionalOnProfile;
import com.netflix.karyon.conditional.annotations.ProvidesConditionally;

/**
 * This module enabled conditional bindings via {@link ProvidesConditionally}.
 */
public class ConditionalSupportModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().scanModulesForAnnotatedMethods(new ModuleAnnotatedMethodScanner() {
            @SuppressWarnings("unchecked")
            @Override
            public Set<? extends Class<? extends Annotation>> annotationClasses() {
                return Sets.newHashSet(ProvidesConditionally.class);
            }

            @Override
            public <T> Key<T> prepareMethod(Binder binder,
                    Annotation annotation, Key<T> key,
                    InjectionPoint injectionPoint) {
                
                ProvidesConditionally conditionally = (ProvidesConditionally)annotation;
                
                Method m = (Method) injectionPoint.getMember();
                List<Annotation> annotations = new ArrayList<>();
                for (Annotation annot : m.getAnnotations()) {
                    if (null != annot.annotationType().getAnnotation(Conditional.class)) {
                        annotations.add(annot);
                    }
                }
                
                if (annotations.isEmpty()) {
                    binder.addError("Method " + m.toString() + " must have at least one Conditional annotation.");
                }

                binder.install(new ConditionalResolvingProvider<T>(key));
                return new ConditionalBinder<T>(binder, key, conditionally.isDefault(), annotations, injectionPoint.toString()).getIdKey();
            }        
        });
        
        // Add one of these to register the evaluator for each Conditional 
        bind(new TypeLiteral<ConditionalMatcher<ConditionalOnProfile>>() {})
            .to(ConditionalOnProfileMatcher.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
