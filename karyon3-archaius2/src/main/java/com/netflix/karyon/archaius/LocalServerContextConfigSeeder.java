package com.netflix.karyon.archaius;

import java.net.InetAddress;

import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.karyon.ServerContext;

/**
 * Used to seed a configuration layer for ServerContext keys with local server
 * information
 * 
 * @author elandau
 */
@Singleton
public class LocalServerContextConfigSeeder implements ConfigSeeder {

    @Override
    public Config get(Config rootConfig) throws Exception {
        InetAddress IP = InetAddress.getLocalHost();
        return MapConfig.builder()
            // Amazon specific metadata
            .put(ServerContext.DOMAIN,          "")
            .put(ServerContext.HOSTNAME,        "localhost")
            .put(ServerContext.PUBLIC_HOSTNAME, IP.getHostName())
            .put(ServerContext.PUBLIC_IPV4,     IP.getHostAddress())
            .put(ServerContext.LOCAL_HOSTNAME,  IP.getHostName())
            .put(ServerContext.LOCAL_IPV4,      IP.getHostAddress())
            .put(ServerContext.DATACENTER,      "us-west-1")
            .put(ServerContext.RACK,            "zone-dev")
            .put(ServerContext.SERVER_ID,       "${@hostname}")
            .put(ServerContext.AMI,             "ami-dev")
            
            // Netflix specific metadata
            .put(ServerContext.ENVIRONMENT,     "test")
            .put(ServerContext.CLUSTER,         "cluster-dev")
            .put(ServerContext.ASG,             "asg-dev")    
            .put(ServerContext.STACK,           "")
            .put(ServerContext.APP_ID,          "")
            
            // Redirects for legacy amazon metadata
            .put("@zone",                       "${" + ServerContext.RACK + "}")
            .put("@region",                     "${" + ServerContext.DATACENTER + "}")
            .put("netflix.datacenter",          "cloud")
            .build();
    }
}
