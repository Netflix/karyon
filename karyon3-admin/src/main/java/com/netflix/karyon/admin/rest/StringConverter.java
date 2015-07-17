package com.netflix.karyon.admin.rest;


/**
 * Strategy for converting from a String to a different Type
 * 
 * @author elandau
 */
public interface StringConverter {
    Object convert(String value) throws Exception;
}
