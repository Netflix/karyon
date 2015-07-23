package com.netflix.karyon.admin;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

@Singleton
public class DIGraphResource {
    private final JsonGrapher grapher;
    private final Injector injector;

    @Inject
    public DIGraphResource(Injector injector, JsonGrapher grapher) {
        this.grapher = grapher;
        this.injector = injector;
    }

    public String get() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos);
        
        grapher.setOut(out);
        grapher.graph(injector);
        return baos.toString("UTF-8");
    }
}
