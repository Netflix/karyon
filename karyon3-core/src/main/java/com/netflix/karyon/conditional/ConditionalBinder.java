package com.netflix.karyon.conditional;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;

/**
 * Binding for a single conditional T.
 * 
 * @param <T>
 */
public class ConditionalBinder<T> {
    private static final AtomicInteger counter = new AtomicInteger();
    
    /**
     * Used to generate unique bindings for conditional T keys that will not conflict
     * with annotated T
     * @return Unique annotation
     */
    public static IdQualifier newIdQualifier() {
        final int id = counter.incrementAndGet();
        return new IdQualifier() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return IdQualifier.class;
            }

            @Override
            public int id() {
                return id;
            }
            
            @Override
            public String toString() {
                return "IdQualifier(id=" + id + ")";
            }
        };
    }
    
    private final IdQualifier       id;
    private final Key<T>            idKey;
    private final List<Annotation>  conditionals;
    private final Object            source;
    private final boolean           isDefault;
    
    public ConditionalBinder(Binder binder, Key<T> key, boolean isDefault, List<Annotation> conditionals, Object source) {
        this.id = newIdQualifier();
        this.idKey = Key.get(key.getTypeLiteral(), id);
        this.conditionals = conditionals;
        this.source = source;
        this.isDefault = isDefault;
        
        TypeLiteral<ConditionalBinder<T>> providerType = (TypeLiteral<ConditionalBinder<T>>) TypeLiteral.get(Types.newParameterizedType(
                    ConditionalBinder.class, 
                    key.getTypeLiteral().getRawType()));
        
        Multibinder<ConditionalBinder<T>> mapBinder = key.getAnnotation() == null
                ? Multibinder.newSetBinder(binder, providerType)
                : Multibinder.newSetBinder(binder, providerType, key.getAnnotation());
                
        mapBinder.addBinding().toInstance(this);
    }

    boolean matches(Injector injector) {
        if (injector == null) {
            throw new ProvisionException("ConditionalBinder has no injector yet");
        }
        
        for (Annotation conditional : conditionals) {
            Key<ConditionalMatcher<?>> key = (Key<ConditionalMatcher<?>>) Key.get(Types.newParameterizedType(ConditionalMatcher.class, conditional.annotationType()));
            try {
                Provider<ConditionalMatcher<?>> foo = (Provider<ConditionalMatcher<?>>) injector.getProvider(key);
                ConditionalMatcher evaluator = (ConditionalMatcher) injector.getInstance(key);
                if (!evaluator.evaluate(conditional)) { 
                    return false;
                }
            }
            catch (Exception e) {
                throw new ProvisionException("Error evaluating matcher " + key + " for conditional " + conditional, e);
            }
        }
        return true;
    }
    
    public T get(Injector injector) {
        return injector.getInstance(idKey);
    }
    
    public Provider<T> getProvider(Injector injector) {
        return injector.getProvider(idKey);
    }

    public Key<T> getIdKey() {
        return idKey;
    }
    
    public Object getSource() {
        return source;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    @Override
    public String toString() {
        return "ConditionalProvider[id=" + id.id() + " at " + source + " isDefault=" + isDefault + "]";
    }

}
