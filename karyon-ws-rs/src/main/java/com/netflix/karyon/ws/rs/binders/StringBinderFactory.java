package com.netflix.karyon.ws.rs.binders;

public interface StringBinderFactory {
    <T> StringBinder<T> create(Class<T> type);
}
