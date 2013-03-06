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

package com.netflix.adminresources;

import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A health check resource available via {@link com.netflix.adminresources.AdminResourcesContainer}, this in turn just
 * calls the {@link HealthCheckInvocationStrategy} to invoke the configured {@link com.netflix.karyon.spi.HealthCheckHandler}. <br/>
 * This servlet is available at path {@link HealthCheckServlet#PATH}
 *
 * @author Nitesh Kant
 */
public class HealthCheckServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServlet.class);
    public static final String PATH = "/healthcheck";

    private HealthCheckInvocationStrategy healthCheckInvocationStrategy;

    public HealthCheckServlet(HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int status = healthCheckInvocationStrategy.invokeCheck();
            resp.setStatus(status);
        } catch (TimeoutException e) {
            logger.error("Karyon health check failed via adminresource health endpoint. Returning 500", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
