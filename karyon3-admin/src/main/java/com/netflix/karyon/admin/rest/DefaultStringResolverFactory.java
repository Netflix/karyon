package com.netflix.karyon.admin.rest;

public class DefaultStringResolverFactory implements StringResolverFactory {

    @Override
    public StringResolver create(Class type) throws Exception {
        try {
            return new ConstructorStringResolver(type.getConstructor(String.class));
        }
        catch (NoSuchMethodException e) {   
            return new ValueOfStringResolver(type.getDeclaredMethod("valueOf", String.class));
        }
    }
}
