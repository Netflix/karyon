package com.netflix.karyon.server.http.servlet.blocking;

import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple holder of name-value pairs to provide attributes for both {@link HttpServletRequestImpl} and
 * {@link HttpSessionImpl}
 *
 * @author Nitesh Kant
 */
class AttributesHolder {

    private ConcurrentHashMap<String, Object> attributes;

    AttributesHolder() {
        attributes = new ConcurrentHashMap<String, Object>();
    }

    Object get(String name) {
        return attributes.get(name);
    }

    Enumeration<String> getAttributeNames() {
        return attributes.keys();
    }

    String[] getAttributeNamesAsArray() {
        Set<String> attribNames = attributes.keySet();
        return attribNames.toArray(new String[attribNames.size()]);
    }

    Object put(String name, Object value) {
        return attributes.put(name, value);
    }

    Object remove(String name) {
        return attributes.remove(name);
    }

    void clear() {
        attributes.clear();
    }
}
