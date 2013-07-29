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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Nitesh Kant
 */
@Path("/webadmin/env")
@Beta
@Produces(MediaType.APPLICATION_JSON)
public class EnvironmentResource {
    @GET
    public Response getAllProperties() {  
    	// make a writable copy of the immutable System.getenv() map
        Map<String,String> envVarsMap = new HashMap<String,String>(System.getenv());
        
        // mask the specified properties if they're in the envVarsMap
        Set<String> maskedResources = MaskedResourceHelper.getMaskedResourceSet();
    	Iterator<String> maskedResourcesIter = maskedResources.iterator();		
    	while (maskedResourcesIter.hasNext()) {			
    		String maskedResource = maskedResourcesIter.next();
        	if (envVarsMap.containsKey(maskedResource)) {
        		envVarsMap.put(maskedResource, MaskedResourceHelper.MASKED_PROPERTY_VALUE);
        	}
        }

        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();                
        String propsJson = gson.toJson(new PairResponse(envVarsMap));        
        
        return Response.ok(propsJson).build();
    }
}
