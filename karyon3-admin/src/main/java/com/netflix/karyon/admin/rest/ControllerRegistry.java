package com.netflix.karyon.admin.rest;

import java.util.List;
import java.util.Set;

/**
 * Registry to access controllers.
 * 
 * @author elandau
 *
 */
public interface ControllerRegistry {

    /**
     * Invoke a controller by providing the path parts. 
     * 
     * @param parts
     * @return
     * @throws Exception
     */
    Object invoke(String controller, List<String> parts) throws Exception;

    Set<String> getNames();

    List<String> getActions(String name);

}
