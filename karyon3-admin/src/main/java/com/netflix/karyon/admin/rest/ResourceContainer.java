package com.netflix.karyon.admin.rest;

import java.util.List;
import java.util.Set;

/**
 * Registry to access controllers.
 * 
 * @author elandau
 *
 */
public interface ResourceContainer {

    /**
     * Invoke a resource by providing the path parts. 
     * 
     * @param parts
     * @return
     * @throws Exception
     */
    Object invoke(String resource, List<String> parts) throws Exception;

    /**
     * Return names of all resources
     * @return
     */
    Set<String> getNames();

}
