package com.netflix.karyon.admin.rest;

import java.lang.reflect.Constructor;

public class ConstructorStringResolver implements StringResolver {
    private Constructor<?> ctor;

    public ConstructorStringResolver(Constructor<?> ctor) {
        this.ctor = ctor;
    }
    
    @Override
    public Object convert(String value) throws Exception {
        return ctor.newInstance(value);
    }
}
