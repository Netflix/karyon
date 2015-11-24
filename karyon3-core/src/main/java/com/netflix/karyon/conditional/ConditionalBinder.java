package com.netflix.karyon.conditional;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
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
     * @return
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
    
    @Inject
    private Injector injector;
    
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

    boolean matches() {
        for (Annotation conditional : conditionals) {
            ConditionalMatcher evaluator = (ConditionalMatcher) injector.getInstance(Key.get(Types.newParameterizedType(ConditionalMatcher.class, conditional.annotationType())));
            if (!evaluator.evaluate(conditional)) { 
                return false;
            }
        }
        return true;
    }
    
    public T get() {
        return injector.getInstance(idKey);
    }
    
    public Provider<T> getProvider() {
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
