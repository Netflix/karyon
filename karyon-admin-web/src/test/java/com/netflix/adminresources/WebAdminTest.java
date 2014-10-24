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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import com.netflix.adminresources.resources.MaskedResourceHelper;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.health.AlwaysHealthyHealthCheck;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheckInvocationStrategy;
import com.netflix.karyon.health.SyncHealthCheckInvocationStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Amit Joshi
 */
public class WebAdminTest {

    private static final Logger LOG = LoggerFactory.getLogger(WebAdminTest.class);

    private static AdminResourcesContainer container;

    private static final Map<String, String> REST_END_POINTS = new ImmutableMap.Builder<String, String>()
            .put("http://localhost:8077/webadmin/props", MediaType.APPLICATION_JSON)
            .put("http://localhost:8077/admin/props", MediaType.TEXT_HTML)
            .put("http://localhost:8077/admin/env", MediaType.TEXT_HTML)
            .put("http://localhost:8077/webadmin/env", MediaType.APPLICATION_JSON)
            .put("http://localhost:8077/admin/jars", MediaType.TEXT_HTML)
            .put("http://localhost:8077/webadmin/jars", MediaType.APPLICATION_JSON)
            .put("http://localhost:8077/webadmin/jmx?key=root&_=1366497431351", MediaType.APPLICATION_JSON)
            .put("http://localhost:8077/admin/jmx", MediaType.TEXT_HTML)
            .put("http://localhost:8077/admin/eureka", MediaType.TEXT_HTML)
            .put("http://localhost:8077/webadmin/eureka", MediaType.APPLICATION_JSON)
            .build();

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("AWS_SECRET_KEY", "super-secret-aws-key");
        System.setProperty("AWS_ACCESS_ID", "super-aws-access-id");
        System.setProperty(MaskedResourceHelper.MASKED_PROPERTY_NAMES, "AWS_SECRET_KEY");

        startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty("AWS_SECRET_KEY");
        ConfigurationManager.getConfigInstance().clearProperty("AWS_ACCESS_ID");
        ConfigurationManager.getConfigInstance().clearProperty(MaskedResourceHelper.MASKED_PROPERTY_NAMES);

        if (container != null) {
            container.shutdown();
        }
    }

    @Test
    public void testRestEndPoints() throws Exception {
        HttpClient client = new DefaultHttpClient();
        for (Map.Entry<String, String> restEndPoint : REST_END_POINTS.entrySet()) {
            final String endPoint = restEndPoint.getKey();
            LOG.info("REST endpoint " + endPoint);
            HttpGet restGet = new HttpGet(endPoint);
            HttpResponse response = client.execute(restGet);
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals(restEndPoint.getValue(), response.getEntity().getContentType().getValue());
            
            // need to consume full response before make another rest call with
            // the default SingleClientConnManager used with DefaultHttpClient
            EntityUtils.consume(response.getEntity());
        }
    }
    
    @Test
    public void testMaskedResources() throws Exception {
        HttpClient client = new DefaultHttpClient();
    	final String endPoint = "http://localhost:8077/webadmin/props";
        LOG.info("REST endpoint " + endPoint);
        HttpGet restGet = new HttpGet(endPoint);
        HttpResponse response = client.execute(restGet);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getEntity().getContentType().getValue());

    	String responseStr = EntityUtils.toString(response.getEntity());
    	LOG.info("responseStr: " + responseStr);
        assertTrue(responseStr.contains("[\"AWS_SECRET_KEY\",\"" + MaskedResourceHelper.MASKED_PROPERTY_VALUE + "\"]"));
        assertTrue(responseStr.contains("[\"AWS_ACCESS_ID\",\"super-aws-access-id\"]"));

        // need to consume full response before make another rest call with
        // the default SingleClientConnManager used with DefaultHttpClient
        EntityUtils.consume(response.getEntity());
    }
        
    private static void startServer() throws Exception {
        container = new AdminResourcesContainer(new Provider<HealthCheckInvocationStrategy>() {
            @Override
            public HealthCheckInvocationStrategy get() {
                return new SyncHealthCheckInvocationStrategy(AlwaysHealthyHealthCheck.INSTANCE);
            }
        }, new Provider<HealthCheckHandler>() {
            @Override
            public HealthCheckHandler get() {
                return AlwaysHealthyHealthCheck.INSTANCE;
            }
        });
        container.init();
    }
}
