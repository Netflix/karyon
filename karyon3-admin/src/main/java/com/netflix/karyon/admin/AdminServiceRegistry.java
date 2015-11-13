package com.netflix.karyon.admin;

import java.util.Set;

public interface AdminServiceRegistry {
    Set<String> getServiceNames();
    Class<?> getServiceClass(String serviceName);
    Object getService(String serviceName);
}
