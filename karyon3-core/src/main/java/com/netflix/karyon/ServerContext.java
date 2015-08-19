package com.netflix.karyon;

/**
 * Standardized set of properties 
 * 
 * @author elandau
 */
public final class ServerContext {
    public static final String DOMAIN          = "@domain";
    public static final String HOSTNAME        = "@hostname";
    public static final String PUBLIC_HOSTNAME = "@publicHostname";
    public static final String PUBLIC_IPV4     = "@publicIpv4";
    public static final String LOCAL_HOSTNAME  = "@localHostname";
    public static final String LOCAL_IPV4      = "@localIpv4";
    public static final String DATACENTER      = "@datacenter";     // 'region' on EC2
    public static final String RACK            = "@rack";           // 'zone' on EC2
    public static final String CLUSTER         = "@cluster";
    public static final String AMI             = "@ami";
    public static final String ASG             = "@asg";    
    public static final String SERVER_ID       = "@serverId";       // i-
    public static final String STACK           = "@stack";
    public static final String ENVIRONMENT     = "@environment";    // test, prod, ..
    public static final String APP_ID          = "@appId";
    public static final String COUNTRIES       = "@countries";
}
