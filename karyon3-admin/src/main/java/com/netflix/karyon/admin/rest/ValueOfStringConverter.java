package com.netflix.karyon.admin.rest;

import java.lang.reflect.Method;

public class ValueOfStringConverter implements StringConverter {
    private Method method;

    public ValueOfStringConverter(Method method) {
        this.method = method;
    }
    
    @Override
    public Object convert(String value) throws Exception {
        return method.invoke(null, value);
    }
}