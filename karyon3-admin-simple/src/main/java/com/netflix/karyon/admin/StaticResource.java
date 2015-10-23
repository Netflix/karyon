package com.netflix.karyon.admin;

/**
 * Encapsulate a resource loaded in {@link StaticResourceProvider} 
 * 
 * @author elandau
 */
public class StaticResource {
    private final String mimeType;
    private final byte[] data;

    public static final StaticResource INVALID = new StaticResource(null, null);
    
    public StaticResource(byte[] data, String mimeType) {
        this.mimeType = mimeType;
        this.data = data;
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isValid() {
        return data != null;
    }

}
