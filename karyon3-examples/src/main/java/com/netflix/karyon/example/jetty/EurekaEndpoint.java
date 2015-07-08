package com.netflix.karyon.example.jetty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

@Path("eureka")
@Produces({MediaType.TEXT_HTML})
public class EurekaEndpoint {
    private final EurekaClient client;

    @Inject
    public EurekaEndpoint(EurekaClient client) {
        this.client = client;
    }
    
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response getApps() {
        StringBuilder sb = new StringBuilder();
        for (Application app : client.getApplications().getRegisteredApplications()) {
            sb.append(app.getName());
            if (!app.getInstances().isEmpty()) {
                sb.append("=").append(app.getInstances().get(0).getVIPAddress()).append("\n");
            }
        }
        
        return Response.ok(sb.toString()).build();
    }
}
