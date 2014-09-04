package com.netflix.karyon.ws.rs.binders;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DefaultStringBinderFactory implements StringBinderFactory {
    @Override
    public <T> StringBinder<T> create(final Class<T> type) {
        if (type.equals(String.class)) {
            return new StringBinder<T>() {
                @SuppressWarnings("unchecked")
                @Override
                public T bind(String value) {
                    return (T) value;
                }
            };
        }
        
        final Method method;
        try {
            method = type.getMethod("valueOf", String.class);
            if (method != null) {
                return new StringBinder<T>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public T bind(String value) {
                        try {
                            return (T) method.invoke(null, value);
                        } catch (Exception e) {
                            throw new RuntimeException("Error parsing input", e);
                        }
                    }
                };
            }
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        
        try {
            final Constructor<T> constructor = type.getConstructor(String.class);
            if (constructor != null) {
                return new StringBinder<T>() {
                    @Override
                    public T bind(String value) {
                        try {
                            return (T) constructor.newInstance(value);
                        } catch (Exception e) {
                            throw new RuntimeException("Error parsing input", e);
                        }
                    }
                };
            }
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        return new StringBinder() {
            @Override
            public Object bind(String value) {
                return null;
            }
        };
    }
}
