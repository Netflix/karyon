package com.netflix.karyon.api;

/**
 * Standardized set of properties 
 * 
 * karyon.author elandau
 */
public final class ServerContext {
    public static final String DOMAIN          = "karyon.domain";
    public static final String HOSTNAME        = "karyon.hostname";
    public static final String PUBLIC_HOSTNAME = "karyon.publicHostname";
    public static final String PUBLIC_IPV4     = "karyon.publicIpv4";
    public static final String LOCAL_HOSTNAME  = "karyon.localHostname";
    public static final String LOCAL_IPV4      = "karyon.localIpv4";
    public static final String DATACENTER      = "karyon.datacenter";     // 'cloud'
    public static final String REGION          = "karyon.region";         // 'us-east', ..
    public static final String ZONE            = "karyon.zoneId";         // 'zone' on EC2
    public static final String CLUSTER         = "karyon.cluster";
    public static final String AMI             = "karyon.ami";
    public static final String ASG             = "karyon.asg";    
    public static final String SERVER_ID       = "karyon.serverId";       // i-
    public static final String STACK           = "karyon.stack";
    public static final String ENVIRONMENT     = "karyon.environment";    // test, prod, ..
    public static final String APP_ID          = "karyon.appId";
    public static final String COUNTRIES       = "karyon.countries";
}
