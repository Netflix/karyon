package com.netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.AbstractConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nitesh Kant
 */
@Beta
@Path("/admin/properties")
@Produces(MediaType.APPLICATION_JSON)
public class PropertiesResource {

    @GET
    public Response getAllProperties() {
        Map<String, String> allPropsAsString = new TreeMap<String, String>();
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = config.getProperty(key);
            if (null != value) {
                allPropsAsString.put(key, value.toString());
            }
        }
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(allPropsAsString);
        return Response.ok(propsJson).build();
    }
}
