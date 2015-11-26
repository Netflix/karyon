package com.netflix.karyon.spi;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;

public abstract class AbstractAutoBinder implements AutoBinder {
    private final Matcher<TypeLiteral<?>> matcher;
    
    public AbstractAutoBinder(Matcher<TypeLiteral<?>> matcher) {
        this.matcher = matcher;
    }
    
    @Override
    public boolean matches(TypeLiteral<?> t) {
        return matcher.matches(t);
    }
}
