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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.MediaType;

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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.netflix.adminresources.resources.MaskedResourceHelper;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.spi.PropertyNames;

/**
 * @author Amit Joshi
 */
public class WebAdminTest {
    private static final Logger LOG = LoggerFactory.getLogger(WebAdminTest.class);

    private static KaryonServer server;

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
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.netflix.adminresources");
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
        System.setProperty("AWS_SECRET_KEY", "super-secret-aws-key");
        System.setProperty("AWS_ACCESS_ID", "super-aws-access-id");
        System.setProperty(MaskedResourceHelper.MASKED_PROPERTY_NAMES, "AWS_SECRET_KEY");

        startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);        
        ConfigurationManager.getConfigInstance().clearProperty("AWS_SECRET_KEY");
        ConfigurationManager.getConfigInstance().clearProperty("AWS_ACCESS_ID");
        ConfigurationManager.getConfigInstance().clearProperty(MaskedResourceHelper.MASKED_PROPERTY_NAMES);

        if (server != null) {
            server.close();
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
    	assertTrue(responseStr.contains("{\"name\":\"AWS_SECRET_KEY\",\"value\":\"" + MaskedResourceHelper.MASKED_PROPERTY_VALUE + "\"}"));
    	assertTrue(responseStr.contains("{\"name\":\"AWS_ACCESS_ID\",\"value\":\"super-aws-access-id\"}"));

        // need to consume full response before make another rest call with
        // the default SingleClientConnManager used with DefaultHttpClient
        EntityUtils.consume(response.getEntity());
    }
        
    private static Injector startServer() throws Exception {
        server = new KaryonServer();
        Injector injector = server.initialize();
        server.start();
        return injector;
    }
}
