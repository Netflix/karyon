package com.netflix.karyon.admin.rest;

import java.lang.reflect.Constructor;

public class ConstructorStringConverter implements StringConverter {
    private Constructor<?> ctor;

    public ConstructorStringConverter(Constructor<?> ctor) {
        this.ctor = ctor;
    }
    
    @Override
    public Object convert(String value) throws Exception {
        return ctor.newInstance(value);
    }
}
