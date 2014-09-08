package com.netflix.karyon.ws.rs.readers;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.base.Charsets;

@Consumes({MediaType.APPLICATION_JSON})
public class JsonRequestReader<T> implements RequestReader<T> {
    private ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            return true;
        }
        return false;
    }

    @Override
    public Observable<T> readFrom(final Class<T> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            HttpServerRequest<ByteBuf> request) throws IOException,
            WebApplicationException {
        
        return request.getContent()
                .flatMap(new Func1<ByteBuf, Observable<T>>() {
                    @Override
                    public Observable<T> call(ByteBuf t1) {
                        try {
                            return Observable.just(mapper.readValue(t1.toString(Charsets.UTF_8), type));
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                });
    }
}
