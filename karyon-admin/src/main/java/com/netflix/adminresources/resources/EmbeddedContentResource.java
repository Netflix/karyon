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
     * The base-explorer framework expects modules in the form of .jar files.
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
        byte[] buffer = null;
        if (mediaType != null) {
            if (resourceCache.containsKey(path)) {
                buffer = resourceCache.get(path);
            } else {
                getClass().getResource(path);
                //TODO: Fix this
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    try {
                        int length = is.available();
                        if (length > 0) {
                            buffer = new byte[length];
                            is.read(buffer, 0, length);
                            // cache so we don't load from classpath each time
                            byte[] existing = resourceCache.putIfAbsent(path, buffer);
                            if (null != existing) {
                                buffer = existing;
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error loading resource", e);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (buffer == null)
            throw new NotFoundException();
        else
            return Response.ok(buffer, mediaType).build();
    }
}
