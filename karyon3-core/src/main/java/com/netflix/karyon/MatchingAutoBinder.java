package com.netflix.karyon;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.netflix.karyon.spi.AutoBinder;

class MatchingAutoBinder<T extends TypeLiteral<?>> implements AutoBinder {
    final Matcher<T> matcher;
    final AutoBinder factory;

    MatchingAutoBinder(Matcher<T> matcher, AutoBinder factory) {
        this.matcher = matcher;
        this.factory = factory;
    }
    
    @Override
    public <K> boolean configure(Binder binder, Key<K> key) {
        if (matcher.matches((T) key.getTypeLiteral())) {
            return factory.configure(binder, key);
        }
        return false;
    }
}
