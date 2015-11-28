package com.netflix.karyon.spi;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.matcher.Matcher;
import com.google.inject.name.Named;
import com.netflix.karyon.KeyMatchers;

public abstract class AbstractNamedAutoBinder<S> extends AbstractAutoBinder {

    public AbstractNamedAutoBinder(Matcher<Key<?>> matcher) {
        super(matcher.and(KeyMatchers.qualifiedWith(Named.class)));
    }

    @Override
    final public <T> Module getModuleForKey(Key<T> key) {
        String name = ((Named) key.getAnnotation()).value();
        return configure((Key<S>)key, name);
    }

    protected abstract  Module configure(Key<S> key, String name);
}
