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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.karyon.server.utils.KaryonUtils;
import com.netflix.karyon.spi.PropertyNames;
import com.sun.jersey.api.view.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pkamath
 * @author Nitesh Kant
 */
@Beta
@Path("/webadmin/eureka")
@Produces(MediaType.APPLICATION_JSON)
public class EurekaResource {

    private static final Logger logger = LoggerFactory.getLogger(JarsInfoResource.class);


    @GET
    public Response getEurekaDetails() {
        List<EurekaInstanceInfo> instanceInfoList = new ArrayList<EurekaInstanceInfo>();

        if (!KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            logger.info("Eureka is not enabled, so not fetching eureka details.");
        } else {
            DiscoveryClient discoveryClient = DiscoveryManager.getInstance().getDiscoveryClient();
            if (null != discoveryClient) {
                Applications apps = discoveryClient.getApplications();
                for (Application app : apps.getRegisteredApplications()) {
                    for (InstanceInfo inst : app.getInstances()) {
                        instanceInfoList.add(new EurekaInstanceInfo(inst.getAppName(), inst.getId(), inst.getStatus().name(), inst.getIPAddr(), inst.getHostName()));
                    }
                }
            }
        }

        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String response = gson.toJson(new KaryonAdminResponse(instanceInfoList));
        return Response.ok(response).build();
    }

    private static class EurekaInstanceInfo {

        private String application;
        private String id;
        private String status;
        private String ipAddress;
        private String hostName;

        private EurekaInstanceInfo(String application, String id, String status, String ipAddress, String hostName) {
            this.application = application;
            this.id = id;
            this.status = status;
            this.ipAddress = ipAddress;
            this.hostName = hostName;
        }

        public String getApplication() {
            return application;
        }

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getHostName() {
            return hostName;
        }
    }

}
