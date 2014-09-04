package com.netflix.karyon.ws.rs.rx;

import org.junit.Test;

import com.netflix.karyon.ws.rs.test.JsonResource;

import rx.Observable;
import rx.functions.Action1;

public class RxReflectionTest {
    @Test
    public void testSubclasses() {
        Observable
            .just(JsonResource.class)
            .flatMap(RxReflection.getAllSubclasses())
            .subscribe(new Action1<Class<?>>() {
                @Override
                public void call(Class<?> t1) {
                    System.out.println(t1.getName());
                }
            });
    }
}
