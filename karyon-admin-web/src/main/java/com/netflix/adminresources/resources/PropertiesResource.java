/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.sun.jersey.api.view.Viewable;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nitesh Kant
 */
@Beta
@Path("/webadmin/props")
@Produces(MediaType.APPLICATION_JSON)
public class PropertiesResource {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesResource.class);

    /**
     * List all properties
    @GET
    public Viewable showIndex() {
        Map<String, Object> model = new HashMap<String, Object>();
        return new Viewable("/webadmin/props/index.ftl", model);
    }
     */

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
        String propsJson = gson.toJson(new PairResponse(allPropsAsString));
        return Response.ok(propsJson).build();
    }

}
