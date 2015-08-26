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
            .put(ServerContext.DOMAIN,          "${EC2_DOMAIN}")
            .put(ServerContext.HOSTNAME,        "${EC2_HOSTNAME}")
            .put(ServerContext.PUBLIC_HOSTNAME, "${EC2_PUBLIC_HOSTNAME}")
            .put(ServerContext.PUBLIC_IPV4,     "${EC2_PUBLIC_IPV4}")
            .put(ServerContext.LOCAL_HOSTNAME,  "${EC2_LOCAL_HOSTNAME}")
            .put(ServerContext.LOCAL_IPV4,      "${EC2_LOCAL_IPV4}")
            .put(ServerContext.DATACENTER,      "cloud")
            .put(ServerContext.ZONE,            "${EC2_AVAILABILITY_ZONE}")
            .put(ServerContext.SERVER_ID,       "${EC2_INSTANCE_ID}")
            .put(ServerContext.AMI,             "${EC2_AMI_ID}")
            .put(ServerContext.REGION,          "${EC2_REGION}")
            .build();
    }
}
