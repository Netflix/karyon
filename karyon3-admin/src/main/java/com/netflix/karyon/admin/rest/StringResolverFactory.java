package com.netflix.karyon.admin.rest;

public interface StringResolverFactory {
    StringResolver create(Class type) throws Exception;
}
