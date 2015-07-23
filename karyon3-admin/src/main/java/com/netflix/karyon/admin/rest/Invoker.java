package com.netflix.karyon.admin.rest;

import java.util.List;

public interface Invoker {
    /**
     * Invoke the method with the provided list of arguments
     * 
     * TOOD: Optional (query parameters)
     * 
     * @param args
     * @return
     * @throws Exception 
     */
    Object invoke(List<String> args) throws Exception;
}
