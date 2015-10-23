package com.netflix.karyon.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;

import javax.activation.MimetypesFileTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.IOUtils;

import com.google.common.net.MediaType;

/**
 * Load resources from a path in the file system
 * 
 * @author elandau
 */
public class FileSystemResourceProvider implements StaticResourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemResourceProvider.class);
    
    private final String rootPath;
    private final MimetypesFileTypeMap mimeTypes;
    
    public FileSystemResourceProvider(String rootPath, String mimeTypesResourceName) {
        this.rootPath = rootPath;
        
        try (InputStream is = this.getClass().getResourceAsStream(mimeTypesResourceName)) {
            this.mimeTypes = new MimetypesFileTypeMap(is);
        } 
        catch (IOException e) {
            throw new RuntimeException("Unable to load mimetype definition file " + mimeTypesResourceName, e);
        }
    }
    
    @Override
    public CompletableFuture<StaticResource> getResource(String name) {
        String filename = getSanitizedResourcePath(name);
        try {
            try (final InputStream is = this.getClass().getResourceAsStream(filename)) {
                try (final Reader reader = new InputStreamReader(is)) {
                    String mimeType  = mimeTypes.getContentType(filename);
                    // Instead of using the default application/octet-stream use text/html
                    if (MediaType.OCTET_STREAM.equals(mimeType)) {
                        mimeType = MediaType.HTML_UTF_8.toString();
                    }
    
                    return CompletableFuture.completedFuture(new StaticResource(IOUtils.readFully(is, -1, true), mimeType));
                }
            }
        }
        catch (NullPointerException | IOException e) {
            LOG.debug("Unable to load resource {}", filename, e);
            return CompletableFuture.completedFuture(StaticResource.INVALID);
        }
    }

    private String getSanitizedResourcePath(String name) {
        // TODO: Make sure the resolved path is 'safe'
        return String.format("/%s%s", rootPath, name);
    }
}
