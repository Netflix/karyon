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

package com.netflix.karyon.util;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nitesh Kant
 */
public class EurekaResourceMock {

    public static final int EUREKA_PORT = 5643;
    public static final String EUREKA_KARYON_APP_NAME = "KARYON-EUREKA-TEST";
    public static final String EUREKA_KARYON_VIP = "karyon-eureka-test.mydomain.net";
    public static final String EUREKA_API_BASE_PATH = "/discovery/v2/";
    public static final String EUREKA_SERVICE_URL = "http://localhost:" + EUREKA_PORT + EUREKA_API_BASE_PATH;
    private Server server;
    public MockHandler handler;

    public void start() {
        server = new Server(EUREKA_PORT);
        handler = new MockHandler();
        server.setHandler(handler);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CannedResponses {

        private static String BLAH_APP_XML = "  <application>\n" +
                                             "    <name>BLAH</name>\n" +
                                             "    <instance>\n" +
                                             "      <hostName>mymachine.abc.com</hostName>\n" +
                                             "      <app>BLAH</app>\n" +
                                             "      <ipAddr>10.10.10.10</ipAddr>\n" +
                                             "      <sid>1</sid>\n" +
                                             "      <status>UP</status>\n" +
                                             "      <overriddenstatus>UNKNOWN</overriddenstatus>\n" +
                                             "      <port enabled=\"true\">80</port>\n" +
                                             "      <securePort enabled=\"false\">443</securePort>\n" +
                                             "      <countryId>1</countryId>\n" +
                                             "      <dataCenterInfo class=\"com.netflix.appinfo.AmazonInfo\">\n" +
                                             "        <name>Amazon</name>\n" +
                                             "        <metadata>\n" +
                                             "          <availability-zone>us-east-1e</availability-zone>\n" +
                                             "          <instance-id>i-xxxxxxx</instance-id>\n" +
                                             "          <public-ipv4>10.10.10.10</public-ipv4>\n" +
                                             "          <public-hostname>ec2-10-10-10-10.compute-1.amazonaws.com</public-hostname>\n" +
                                             "          <local-ipv4>10.10.10.10</local-ipv4>\n" +
                                             "          <ami-id>ami-xxxxxxx</ami-id>\n" +
                                             "          <instance-type>m2.4xlarge</instance-type>\n" +
                                             "        </metadata>\n" +
                                             "      </dataCenterInfo>\n" +
                                             "      <leaseInfo>\n" +
                                             "        <renewalIntervalInSecs>1</renewalIntervalInSecs>\n" +
                                             "        <durationInSecs>90</durationInSecs>\n" +
                                             "        <registrationTimestamp>1360619077753</registrationTimestamp>\n" +
                                             "        <lastRenewalTimestamp>1360619077753</lastRenewalTimestamp>\n" +
                                             "        <evictionTimestamp>0</evictionTimestamp>\n" +
                                             "      </leaseInfo>\n" +
                                             "      <metadata class=\"java.util.Collections$EmptyMap\"/>\n" +
                                             "      <homePageUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/Status</homePageUrl>\n"
                                             +
                                             "      <statusPageUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/Status</statusPageUrl>\n"
                                             +
                                             "      <healthCheckUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/healthcheck</healthCheckUrl>\n"
                                             +
                                             "      <vipAddress>blah_app_vip:80</vipAddress>\n" +
                                             "      <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>\n" +
                                             "      <lastUpdatedTimestamp>1360619077753</lastUpdatedTimestamp>\n" +
                                             "      <lastDirtyTimestamp>1360377164649</lastDirtyTimestamp>\n" +
                                             "      <actionType>ADDED</actionType>\n" +
                                             "      <asgName>blah-app_asg-v008</asgName>\n" +
                                             "    </instance>\n" +
                                             "  </application>\n";

        private static String apps() {
            return "<applications>\n" +
                   "  <versions__delta>1</versions__delta>\n" +
                   "  <apps__hashcode>UP_1_</apps__hashcode>\n" +
                   BLAH_APP_XML +
                   "</applications>";
        }

        private static String testApp() {
            return "<applications>\n" +
                   "  <versions__delta>1</versions__delta>\n" +
                   "  <apps__hashcode>UP_2_</apps__hashcode>\n" +
                   BLAH_APP_XML +
                   "  <application>\n" +
                   "    <name>KARYON-EUREKA-TEST</name>\n" +
                   "    <instance>\n" +
                   "      <hostName>mymachine.abc.com</hostName>\n" +
                   "      <app>KARYON-EUREKA-TEST</app>\n" +
                   "      <ipAddr>10.10.10.10</ipAddr>\n" +
                   "      <sid>1</sid>\n" +
                   "      <status>UP</status>\n" +
                   "      <overriddenstatus>UNKNOWN</overriddenstatus>\n" +
                   "      <port enabled=\"true\">80</port>\n" +
                   "      <securePort enabled=\"false\">443</securePort>\n" +
                   "      <countryId>1</countryId>\n" +
                   "      <dataCenterInfo class=\"com.netflix.appinfo.AmazonInfo\">\n" +
                   "        <name>Amazon</name>\n" +
                   "        <metadata>\n" +
                   "          <availability-zone>us-east-1e</availability-zone>\n" +
                   "          <instance-id>i-xxxxxxx</instance-id>\n" +
                   "          <public-ipv4>10.10.10.10</public-ipv4>\n" +
                   "          <public-hostname>ec2-10-10-10-10.compute-1.amazonaws.com</public-hostname>\n" +
                   "          <local-ipv4>10.10.10.10</local-ipv4>\n" +
                   "          <ami-id>ami-xxxxxxx</ami-id>\n" +
                   "          <instance-type>m2.4xlarge</instance-type>\n" +
                   "        </metadata>\n" +
                   "      </dataCenterInfo>\n" +
                   "      <leaseInfo>\n" +
                   "        <renewalIntervalInSecs>1</renewalIntervalInSecs>\n" +
                   "        <durationInSecs>90</durationInSecs>\n" +
                   "        <registrationTimestamp>1360619077753</registrationTimestamp>\n" +
                   "        <lastRenewalTimestamp>1360619077753</lastRenewalTimestamp>\n" +
                   "        <evictionTimestamp>0</evictionTimestamp>\n" +
                   "      </leaseInfo>\n" +
                   "      <metadata class=\"java.util.Collections$EmptyMap\"/>\n" +
                   "      <homePageUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/Status</homePageUrl>\n" +
                   "      <statusPageUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/Status</statusPageUrl>\n" +
                   "      <healthCheckUrl>http://ec2-10-10-10-10.compute-1.amazonaws.com:80/healthcheck</healthCheckUrl>\n"
                   +
                   "      <vipAddress>" + EUREKA_KARYON_VIP + "</vipAddress>\n" +
                   "      <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>\n" +
                   "      <lastUpdatedTimestamp>1360619077753</lastUpdatedTimestamp>\n" +
                   "      <lastDirtyTimestamp>1360377164649</lastDirtyTimestamp>\n" +
                   "      <actionType>ADDED</actionType>\n" +
                   "      <asgName>karyon-app_asg-v008</asgName>\n" +
                   "    </instance>\n" +
                   "  </application>\n" +
                   "</applications>";
        }
    }

    public static class MockHandler extends AbstractHandler {

        public AtomicBoolean appRegistered = new AtomicBoolean();

        @Override
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException {
            String pathInfo = request.getPathInfo();
            System.out.println(
                    "Eureka resource mock, received request on path: " + pathInfo + ". HTTP method: |" + request
                            .getMethod() + "|");
            if (null != pathInfo && pathInfo.startsWith(EUREKA_API_BASE_PATH)) {
                pathInfo = pathInfo.substring(EUREKA_API_BASE_PATH.length());
                if (pathInfo.startsWith("apps/" + EUREKA_KARYON_APP_NAME)) {
                    if (request.getMethod().equals("PUT") || request.getMethod().equals("POST")) {
                        appRegistered.set(true);
                        ((Request) request).setHandled(true);
                    } else if (request.getMethod().equals("DELETE")) {
                        appRegistered.set(false);
                        ((Request) request).setHandled(true);
                        System.out.println("Sent response for DELETE");
                    } else {
                        sendOkResponseWithContent((Request) request, response, CannedResponses.testApp());
                    }
                } else if (pathInfo.startsWith("apps")) {
                    if (appRegistered.get()) {
                        sendOkResponseWithContent((Request) request, response, CannedResponses.testApp());
                    } else {
                        sendOkResponseWithContent((Request) request, response, CannedResponses.apps());
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Request path: " + pathInfo + " not supported by eureka resource mock.");
            }
        }

        private void sendOkResponseWithContent(Request request, HttpServletResponse response, String content)
                throws IOException {
            response.setContentType("application/xml");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(content);
            response.getWriter().flush();
            request.setHandled(true);
            System.out.println("Eureka resource mock, sent response for request path: " + request.getPathInfo());
        }
    }
}
