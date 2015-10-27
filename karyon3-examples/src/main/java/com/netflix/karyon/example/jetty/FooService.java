package com.netflix.karyon.example.jetty;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FooService {
    public static AtomicInteger counter = new AtomicInteger();
    
    @Inject
    public FooService() {
        counter.incrementAndGet();
    }
    
    public int getCount() {
        return counter.get();
    }
    
    public double getErrorRate() {
        return new Random().nextDouble();
    }
}
