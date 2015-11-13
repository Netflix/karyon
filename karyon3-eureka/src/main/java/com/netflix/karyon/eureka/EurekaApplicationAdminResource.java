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
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="eureka-apps", index="list")
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
    
    public static class GetRequest {
        String appName;
        
        public String getAppName() {
            return appName;
        }
        
        public void setAppName(String name) {
            this.appName = name;
        }
    }

    public Map<String, Summary> list() {
        return client.getApplications()
                .getRegisteredApplications()
                .stream()
                .collect(Collectors.toMap((app) -> app.getName(), (app) -> new Summary(app)));
    }
    
    public List<String> get(GetRequest request) {
        return client.getApplications()
                .getRegisteredApplications(request.getAppName())
                .getInstances()
                .stream()
                .map((inst) -> inst.getId()).collect(Collectors.toList());
    }
    
    public List<InstanceInfo> getInstances(GetRequest request) {
        return client.getApplications().getRegisteredApplications(request.getAppName()).getInstances();
    }
    
    public Map<String, Long> getAsgs(GetRequest request) {
        return new Summary(client.getApplications().getRegisteredApplications(request.getAppName())).getAsgs();
    }

    public Map<String, Long> getVips(GetRequest request) {
        return new Summary(client.getApplications().getRegisteredApplications(request.getAppName())).getVips();
    }
    
    private static void increment(Map<String, Long> counts, String key) {
        Long count = counts.get(key);
        if (count == null) {
            count = new Long(0L);
        }
        counts.put(key, count+1);
    }

}
