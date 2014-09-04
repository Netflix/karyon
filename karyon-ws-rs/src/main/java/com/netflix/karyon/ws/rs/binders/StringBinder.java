package com.netflix.karyon.ws.rs.binders;

public interface StringBinder<T> {
    T bind(String value);
}
