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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pkamath
 * @author Nitesh Kant
 */
@Beta
@Path("/webadmin/jars")
@Produces(MediaType.APPLICATION_JSON)
public class JarsInfoResource {

    private static final Logger logger = LoggerFactory.getLogger(JarsInfoResource.class);

    private static final String JAR_PATTERN = "^jar:file:(.+)!/META-INF/MANIFEST.MF$";

    @GET
    public Response getAllJarsInfo() {
        List<JarInfo> jarInfo = getJarInfo();
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(new KaryonAdminResponse(jarInfo));
        return Response.ok(propsJson).build();
    }

    private List<JarInfo> getJarInfo() {
        List<JarInfo> toReturn = new ArrayList<JarInfo>();
        Pattern pattern = Pattern.compile(JAR_PATTERN);
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = cl.getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL manifestURL = urls.nextElement();
                InputStream is = manifestURL.openStream();
                String key = manifestURL.toString();
                Matcher matcher = pattern.matcher(key);
                if (matcher.matches()) {
                    key = matcher.group(1);
                }
                Attributes mainAttributes = new Manifest(is).getMainAttributes();
                toReturn.add(new JarInfo(key, mainAttributes));
                is.close();
            }
        } catch (Exception e) {
            logger.error("Failed to load environment jar information.", e);
        }
        return toReturn;
    }

    private static class JarInfo {

        public static final String MANIFEST_VERSION = "Manifest-Version";
        public static final String CREATED_BY = "Created-By";
        public static final String UNAVAILABLE = "Unavailable";
        @SuppressWarnings("unused")
        private String jar;
        @SuppressWarnings("unused")
        private String createdBy = UNAVAILABLE;
        @SuppressWarnings("unused")
        private String manifestVersion = UNAVAILABLE;

        public JarInfo(String key, Attributes mainAttributes) {
            jar = key;
            if (null != mainAttributes.getValue(MANIFEST_VERSION)) {
                manifestVersion = String.valueOf(mainAttributes.getValue(MANIFEST_VERSION));
            }

            if (null != mainAttributes.getValue(CREATED_BY)) {
                createdBy = String.valueOf(mainAttributes.getValue(CREATED_BY));
            }
        }

        public String getJar() {
            return jar;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getManifestVersion() {
            return manifestVersion;
        }
    }
}
