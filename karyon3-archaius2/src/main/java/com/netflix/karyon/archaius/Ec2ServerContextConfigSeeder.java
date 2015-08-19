package com.netflix.karyon.archaius;

import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.karyon.ServerContext;

@Singleton
public class Ec2ServerContextConfigSeeder implements ConfigSeeder {
    @Override
    public Config get(Config config) {
        return MapConfig.builder()
            // Amazon specific metadata
            .put(ServerContext.DOMAIN,          config.getString("EC2_DOMAIN", ""))
            .put(ServerContext.HOSTNAME,        config.getString("EC2_HOSTNAME", ""))
            .put(ServerContext.PUBLIC_HOSTNAME, config.getString("EC2_PUBLIC_HOSTNAME", ""))
            .put(ServerContext.PUBLIC_IPV4,     config.getString("EC2_PUBLIC_IPV4", ""))
            .put(ServerContext.LOCAL_HOSTNAME,  config.getString("EC2_LOCAL_HOSTNAME", ""))
            .put(ServerContext.LOCAL_IPV4,      config.getString("EC2_LOCAL_IPV4", ""))
            .put(ServerContext.DATACENTER,      config.getString("EC2_REGION", ""))
            .put(ServerContext.RACK,            config.getString("EC2_AVAILABILITY_ZONE", ""))
            .put(ServerContext.SERVER_ID,       config.getString("EC2_INSTANCE_ID", ""))
            .put(ServerContext.AMI,             config.getString("EC2_AMI_ID", ""))
            
            // Netflix specific metadata
            .put(ServerContext.ENVIRONMENT,     config.getString("NETFLIX_ENVIRONMENT", ""))
            .put(ServerContext.CLUSTER,         config.getString("NETFLIX_CLUSTER", ""))
            .put(ServerContext.ASG,             config.getString("NETFLIX_AUTO_SCALE_GROUP", ""))
            .put(ServerContext.STACK,           config.getString("NETFLIX_STACK", ""))
            .put(ServerContext.APP_ID,          config.getString("NETFLIX_APP", ""))
            
            // Redirects for legacy amazon metadata
            .put("@zone",                       "${" + ServerContext.RACK + "}")
            .put("@region",                     "${" + ServerContext.DATACENTER + "}")
            .put("netflix.datacenter",          "cloud")
            .build();
    }
}
