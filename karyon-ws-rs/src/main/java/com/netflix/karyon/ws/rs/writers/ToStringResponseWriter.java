package com.netflix.karyon.ws.rs.writers;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import rx.Observable;
import rx.functions.Func1;

@Provider
public class ToStringResponseWriter implements ResponseWriter<Object> {

    @Override
    public boolean isWriteable(Class type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(Object t, Class type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public Observable<Void> write(Observable<?> source, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            final HttpServerResponse<ByteBuf> response) throws IOException,
            WebApplicationException {
        return source.flatMap(new Func1<Object, Observable<Void>>() {
            @Override
            public Observable<Void> call(Object obj) {
                return response.writeStringAndFlush(obj.toString());
            }
        });
    }
}
