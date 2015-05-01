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

package netflix.adminresources.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pkamath
 * @author Nitesh Kant
 */
@Beta
@Path("/jars")
@Produces(MediaType.APPLICATION_JSON)
public class JarsInfoResource {

    private static final Logger logger = LoggerFactory.getLogger(JarsInfoResource.class);

    private static final String JAR_PATTERN = "^jar:file:(.+)!/META-INF/MANIFEST.MF$";

    private final List<JarManifest> jarManifests;
    private final ArrayList<JarInfo> jarInfos;

    public JarsInfoResource() {
        jarManifests = loadJarManifests();
        jarInfos = new ArrayList<>();
        for (JarManifest jm : jarManifests) {
            jarInfos.add(jm.toJarInfo());
        }
    }

    public static class JarsInfoResponse {
        private List<JarInfo> jars;

        public JarsInfoResponse(List<JarInfo> jarInfos) {
            this.jars = jarInfos;
        }

        public List<JarInfo> getJars() {
            return jars;
        }
    }

    @GET
    public Response getAllJarsInfo() {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String jarsJson = gson.toJson(new JarsInfoResponse(jarInfos));
        return Response.ok(jarsJson).build();
    }

    @GET
    @Path("/{id}")
    public Response getJarManifest(@PathParam("id") int jarId) {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(new KaryonAdminResponse(jarManifests.get(jarId)));
        return Response.ok(propsJson).build();
    }

    private static List<JarManifest> loadJarManifests() {
        List<JarManifest> jarManifests = new ArrayList<>();

        Pattern pattern = Pattern.compile(JAR_PATTERN);
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = cl.getResources("META-INF/MANIFEST.MF");
            int id = 0;
            while (urls.hasMoreElements()) {
                URL manifestURL = urls.nextElement();
                InputStream is = manifestURL.openStream();
                String key = manifestURL.toString();
                Matcher matcher = pattern.matcher(key);
                if (matcher.matches()) {
                    key = matcher.group(1);
                }
                jarManifests.add(new JarManifest(id, key, new Manifest(is)));
                is.close();
                id++;
            }
        } catch (Exception e) {
            logger.error("Failed to load environment jar information.", e);
        }

        return jarManifests;
    }

    private static class JarManifest {
        private final int id;
        private final String jarName;
        private final Manifest manifest;

        private JarManifest(int id, String jarName, Manifest manifest) {
            this.id = id;
            this.jarName = jarName;
            this.manifest = manifest;
        }

        public String getJarName() {
            return jarName;
        }

        public Manifest getManifest() {
            return manifest;
        }

        public JarInfo toJarInfo() {
            return new JarInfo(id, jarName, manifest.getMainAttributes());
        }
    }

    private static class JarInfo {
        public static final String LIBRARY_OWNER = "Library-Owner";
        public static final String BUILD_DATE = "Build-Date";
        public static final String STATUS = "Status";
        public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
        public static final String IMPLEMENTATION_TITLE = "Implementation-Title";
        public static final String SPECIFICATION_VERSION = "Specification-Version";
        public static final String UNAVAILABLE = "-";

        private final int id;
        private final String name;
        private final String libraryOwner;
        private final String buildDate;
        private final String status;
        private final String implementationVersion;
        private final String implementationTitle;
        private final String specificationVersion;

        private JarInfo(int id, String jar, Attributes mainAttributes) {
            this.id = id;
            this.name = jar;
            libraryOwner = valueOf(mainAttributes, LIBRARY_OWNER);
            buildDate = valueOf(mainAttributes, BUILD_DATE);
            status = valueOf(mainAttributes, STATUS);
            implementationTitle = valueOf(mainAttributes, IMPLEMENTATION_TITLE);
            implementationVersion = valueOf(mainAttributes, IMPLEMENTATION_VERSION);
            specificationVersion = valueOf(mainAttributes, SPECIFICATION_VERSION);
        }

        public String getStatus() {
            return status;
        }

        public String getLibraryOwner() {
            return libraryOwner;
        }

        public String getBuildDate() {
            return buildDate;
        }

        public String getName() {
            return name;
        }

        public String getImplementationVersion() {
            return implementationVersion;
        }

        public String getImplementationTitle() {
            return implementationTitle;
        }

        public String getSpecificationVersion() {
            return specificationVersion;
        }

        private static String valueOf(Attributes mainAttributes, String tag) {
            String value = mainAttributes.getValue(tag);
            return value == null ? UNAVAILABLE : value;
        }
    }
}
