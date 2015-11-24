package com.netflix.karyon.conditional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.util.Types;

/**
 * Provider for type T that evaluates multiple conditional bindings and returns T iff 
 * exactly one of the conditions is met or a default has been indicated.  
 * 
 * @param <T>
 */
final class ConditionalResolvingProvider<T> implements Module, Provider<T> {
    @Inject
    Injector injector;
    
    private final Key<T> key;
    
    public ConditionalResolvingProvider(Key<T> key) {
        this.key = key;
    }
    
    @Override
    public T get() {
        return resolveProvider().get();
    }
    
    @Override
    public void configure(Binder binder) {
        binder.bind(key).toProvider(this);
    }
    
    public Provider<T> resolveProvider() {
        Type providerType = Types.newParameterizedType(
                ConditionalBinder.class, 
                key.getTypeLiteral().getRawType());
        
        Set<ConditionalBinder<T>> providers = (Set<ConditionalBinder<T>>) injector.getInstance(key.ofType(Types.setOf(providerType)));
        ConditionalBinder<T> defaultProvider = null;
        
        List<ConditionalBinder<T>> matchedBindings = new ArrayList<>();
        for (ConditionalBinder<T> provider : providers) {
            if (provider.matches()) {
                matchedBindings.add(provider);
            }
            else if (provider.isDefault()) {
                if (defaultProvider != null) {
                    throw new ProvisionException("Only one default provider allowed for " + key.getTypeLiteral());
                }
                defaultProvider = provider;
            }
        }
        
        if (matchedBindings.size() == 0) {
            if (defaultProvider != null) {
                return defaultProvider.getProvider();
            }
            else {
                throw new ProvisionException("No binding found for " + key.getTypeLiteral());
            }
        }
        else if (matchedBindings.size() == 1) {
            return matchedBindings.get(0).getProvider();
        }
        else {
            throw new ProvisionException("Multiple (" + matchedBindings.size() + ") bindings found for " + key + "\n. " + matchedBindings);
        }
    }
    
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConditionalResolvingProvider other = (ConditionalResolvingProvider) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
}