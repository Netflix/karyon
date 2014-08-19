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

package com.netflix.hellonoss.server.health;

import com.google.inject.Inject;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

@Path("/healthcheck")
@Singleton
public class HealthcheckResource {

    @Inject(optional = true)
    private HealthCheckInvocationStrategy invocationStrategy;

    @GET
    public Response doHealthCheck() {
        if (null != invocationStrategy) {
            try {
                int status = invocationStrategy.invokeCheck();
                return Response.status(status).build();
            } catch (TimeoutException e) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
