package com.netflix.karyon.example.jetty;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.netflix.archaius.Config;
import com.netflix.archaius.visitor.PrintStreamVisitor;

@Path("config")
@Produces({MediaType.TEXT_HTML})
public class ArchaiusEndpoint {
    private final Config config;

    @Inject
    public ArchaiusEndpoint(Config config) {
        this.config = config;
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response getApps() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        config.accept(new PrintStreamVisitor(ps));
        
        return Response.ok(baos.toString()).build();
    }
}
