package com.netflix.karyon.admin.rest;

import java.lang.reflect.Method;

public class ValueOfStringResolver implements StringResolver {
    private Method method;

    public ValueOfStringResolver(Method method) {
        this.method = method;
    }
    
    @Override
    public Object convert(String value) throws Exception {
        return method.invoke(null, value);
    }
}