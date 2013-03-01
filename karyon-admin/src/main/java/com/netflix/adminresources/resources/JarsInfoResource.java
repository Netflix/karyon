package com.netflix.adminresources.resources;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pkamath
 * @author Nitesh Kant
 */
@Path("/admin/jars")
@Produces(MediaType.APPLICATION_JSON)
public class JarsInfoResource {

    private static final Logger logger = LoggerFactory.getLogger(JarsInfoResource.class);

    private static final String JAR_PATTERN = "^jar:file:(.+)!/META-INF/MANIFEST.MF$";

    @GET
    public Response getAllJarsInfo() {
        Map<String, Attributes> jarInfo = getJarInfo();
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(jarInfo);
        return Response.ok(propsJson).build();
    }

    public Map<String, Attributes> getJarInfo() {
        Map<String, Attributes> jarInfo = new HashMap<String, Attributes>();
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
                jarInfo.put(key, new Manifest(is).getMainAttributes());
                is.close();
            }
        } catch (Exception e) {
            logger.error("Failed to load environment jar information.", e);
        }
        return jarInfo;
    }
}
