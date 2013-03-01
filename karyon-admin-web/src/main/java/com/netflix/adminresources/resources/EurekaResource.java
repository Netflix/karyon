package com.netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.karyon.server.utils.KaryonUtils;
import com.netflix.karyon.spi.PropertyNames;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author pkamath
 * @author Nitesh Kant
 */
@Beta
@Path("/admin/eureka")
@Produces(MediaType.APPLICATION_JSON)
public class EurekaResource {

    private static final Logger logger = LoggerFactory.getLogger(JarsInfoResource.class);

    @GET
    public Response getEurekaDetails() {
        if (!KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            logger.info("Eureka is not enabled, so not fetching eureka details.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        JSONArray instances = new JSONArray();

        DiscoveryClient discoveryClient = DiscoveryManager.getInstance().getDiscoveryClient();
        if (null != discoveryClient) {
            Applications apps = discoveryClient.getApplications();
            for (Application app : apps.getRegisteredApplications()) {
                for (InstanceInfo inst : app.getInstances()) {
                    instances.put(new JSONArray()
                            .put(inst.getAppName())
                            .put(inst.getId())
                            .put(inst.getStatus().name())
                            .put(inst.getIPAddr())
                            .put(inst.getHostName()));
                }
            }
        }
        try {
            JSONObject responseJson = new JSONObject().put("names",
                    new JSONArray().put("app").put("instance").put("status").put("ip").put("hostname").put("updated"))
                                             .put("rows", instances);
            return Response.ok(responseJson.toString()).build();
        } catch (JSONException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

}
