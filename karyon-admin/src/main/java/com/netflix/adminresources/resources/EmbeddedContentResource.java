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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.jersey.api.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentMap;

/**
 * Karyon admin resources infrastructure expects modules in the form of .jar files. These
 * .jar files contain Jersey REST resource java classes and also resources for
 * rendering like *.js, *.jpg, *.css files. The REST classes are discovered by
 * the Jersey container scanning the classpath. To get the other resources, we
 * will use this class to scan the classpath (and hence the module .jar files)
 * and return the bytes corresponding to the contents of the resources files
 * (i.e. the *.js, *.jpg, *.css files)
 * 
 * @author pkamath
 * @author Nitesh Kant
 * 
 */
@Path("/adminres")

public class EmbeddedContentResource {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedContentResource.class);

    private static final ImmutableMap<String, String> EXT_TO_MEDIATYPE = new ImmutableMap.Builder<String, String>()
            .put("js", "text/javascript").put("png", "image/png")
            .put("gif", "image/gif").put("css", "text/css")
            .put("jpg", "image/jpeg").put("jpeg", "image/jpeg")
            .put("html", "text/html").build();

    private ConcurrentMap<String, byte[]> resourceCache = Maps.newConcurrentMap();

    @Context
    private UriInfo uriInfo;

    /**
     * The karyon admin resources can exist in the form of .jar files.
     * These .jar files contain Jersey REST resource java classes and also
     * resources for rendering like *.js, *.jpg, *.css files. The REST classes
     * are discovered by the Jersey container scanning the classpath. To get the
     * other resources, we will use this method to scan the classpath (and hence
     * the module .jar files) and return the bytes corresponding to the contents
     * of the resources files (i.e. the *.js, *.jpg, *.css files)
     */
    // For URIs of the form /adminres/<something>
    @GET
    @Path("{subResources:.*}")
    public Response get() {
        // get the part after /adminres
        String path = uriInfo.getPath().substring("adminres".length());
        String ext = StringUtils.substringAfterLast(path, ".");
        String mediaType = EXT_TO_MEDIATYPE.get(ext);
        byte[] contentAsBytes = null;
        if (mediaType != null) {
            if (resourceCache.containsKey(path)) {
                contentAsBytes = resourceCache.get(path);
            } else {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
                        byte[] chunk = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(chunk)) > 0) {
                            baos.write(chunk, 0, bytesRead);
                        }
                        // This is a rather costly operation as we are throwing away all content loaded in memory, but
                        // this is only the case when the same resource is requested concurrently.
                        contentAsBytes = baos.toByteArray();
                        byte[] existing = resourceCache.putIfAbsent(path, contentAsBytes);
                        if (null != existing) {
                            contentAsBytes = existing;
                        }
                    } catch (IOException e) {
                        logger.error("Error loading resource with path: " + uriInfo.getPath(), e);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            logger.info("Could not close the resource stream for loading admin resource, ignoring.", e);
                        }
                    }
                }
            }
        }

        if (contentAsBytes == null) {
            logger.info("Could not find resource: " + uriInfo.getPath());
            throw new NotFoundException();
        } else {
            return Response.ok(contentAsBytes, mediaType).build();
        }
    }
}
