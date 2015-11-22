package com.netflix.karyon.experimental.admin;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="di-graph", index="list")
final class DIGraphResource {
    private final JsonGrapher grapher;
    private final Injector injector;

    @Inject
    public DIGraphResource(Injector injector, JsonGrapher grapher) {
        this.grapher = grapher;
        this.injector = injector;
    }

    public String list() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos);
        
        grapher.setOut(out);
        grapher.graph(injector);
        return baos.toString("UTF-8");
    }
}
