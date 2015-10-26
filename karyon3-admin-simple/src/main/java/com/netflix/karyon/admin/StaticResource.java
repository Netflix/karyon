package com.netflix.karyon.admin;

/**
 * Encapsulate a resource loaded in {@link StaticResourceProvider} 
 * 
 * @author elandau
 */
public class StaticResource {
    private final String mimeType;
    private final byte[] data;

    public StaticResource(byte[] data, String mimeType) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (mimeType == null) {
            throw new IllegalArgumentException("MimeType cannot be null");
        }
        this.mimeType = mimeType;
        this.data = data;
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return data;
    }
}
