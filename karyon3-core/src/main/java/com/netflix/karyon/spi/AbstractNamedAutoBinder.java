package com.netflix.karyon.spi;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.name.Named;

public abstract class AbstractNamedAutoBinder<S> extends AbstractAutoBinder {

    public AbstractNamedAutoBinder(Matcher<TypeLiteral<?>> matcher) {
        super(matcher);
    }

    @Override
    final public <T> Module getModuleForKey(Key<T> key) {
        if (key.getAnnotation() == null || !key.getAnnotationType().equals(Named.class)) {
            return null;
        }
        
        String name = ((Named) key.getAnnotation()).value();
        return configure((Key<S>)key, name);
    }

    protected abstract  Module configure(Key<S> key, String name);
}
