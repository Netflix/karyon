package com.netflix.karyon.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

@Singleton
public class GuiceAdminServiceRegistry implements AdminServiceRegistry {
    class ServiceDefinition {
        public ServiceDefinition(Class<?> type, Provider<?> provider) {
            this.type = type;
            this.provider = provider;
        }
        
        final Class<?> type;
        final Provider<?> provider;
    }
    
    private Map<String, ServiceDefinition> services = new HashMap<>();
    
    @Inject
    GuiceAdminServiceRegistry(Injector injector) {
        for (Entry<Key<?>, Binding<?>> key : injector.getAllBindings().entrySet()) {
            Class<?> type = key.getKey().getTypeLiteral().getRawType();
            AdminService annot = type.getAnnotation(AdminService.class);
            if (annot != null) {
                services.put(annot.name(), new ServiceDefinition(type, injector.getProvider(type)));
            }
        }
    }
    
    @Override
    public Set<String> getServiceNames() {
        return services.keySet();
    }

    @Override
    public Object getService(String serviceName) {
        return services.get(serviceName).provider.get();
    }

    @Override
    public Class<?> getServiceClass(String serviceName) {
        return services.get(serviceName).type;
    }
    
}
