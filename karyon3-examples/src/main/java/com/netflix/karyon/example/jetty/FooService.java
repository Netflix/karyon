package com.netflix.karyon.example.jetty;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class FooService {
    public double getErrorRate() {
        return new Random().nextDouble();
    }
}
