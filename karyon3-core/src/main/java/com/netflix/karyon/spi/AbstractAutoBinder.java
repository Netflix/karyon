package com.netflix.karyon.spi;

import com.google.inject.Key;
import com.google.inject.matcher.Matcher;

public abstract class AbstractAutoBinder implements AutoBinder {
    private final Matcher<Key<?>> matcher;
    
    public AbstractAutoBinder(Matcher<Key<?>> matcher) {
        this.matcher = matcher;
    }
    
    @Override
    public final boolean matches(Key<?> t) {
        return matcher.matches(t);
    }
}
