package com.netflix.karyon.eureka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;

@Singleton
public class EurekaApplicationAdminResource {
    private DiscoveryClient client;

    @Inject
    public EurekaApplicationAdminResource(DiscoveryClient client) {
        this.client = client;
    }
    
    public static class Summary {
        private Map<String, Long> vipCounts = new HashMap<>();
        private Map<String, Long> asgCounts = new HashMap<>();
        private int instanceCount = 0;
        
        public Summary(Application app) {
            for (InstanceInfo instance : app.getInstances()) {
                for (String vips : new String[]{instance.getVIPAddress(), instance.getSecureVipAddress()}) {
                    if (vips != null) {
                        for (String vip : vips.split(",")) {
                            if (!vip.startsWith(instance.getHostName())) {
                                increment(vipCounts, vip);
                            }
                        }
                    }
                }
                
                String asg = instance.getASGName();
                if (asg != null) {
                    increment(asgCounts, asg);
                }
            }
            
            instanceCount = app.getInstances().size();
        }
        
        public Map<String, Long> getVips() {
            return vipCounts;
        }
        
        public Map<String, Long> getAsgs() {
            return asgCounts;
        }
        
        public int getInstanceCount() {
            return instanceCount;
        }
    }
    
    public Map<String, Summary> get() {
        return client.getApplications()
                .getRegisteredApplications()
                .stream()
                .collect(Collectors.toMap((app) -> app.getName(), (app) -> new Summary(app)));
    }
    
    public List<String> get(String appName) {
        return client.getApplications()
                .getRegisteredApplications(appName)
                .getInstances()
                .stream()
                .map((inst) -> inst.getId()).collect(Collectors.toList());
    }
    
    public List<InstanceInfo> getInstances(String appName) {
        return client.getApplications().getRegisteredApplications(appName).getInstances();
    }
    
    public Map<String, Long> getAsgs(String appName) {
        return new Summary(client.getApplications().getRegisteredApplications(appName)).getAsgs();
    }

    public Map<String, Long> getVips(String appName) {
        return new Summary(client.getApplications().getRegisteredApplications(appName)).getVips();
    }
    
    private static void increment(Map<String, Long> counts, String key) {
        Long count = counts.get(key);
        if (count == null) {
            count = new Long(0L);
        }
        counts.put(key, count+1);
    }

}
