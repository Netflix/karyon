package com.netflix.karyon.spi;

/**
 * This is an extension to the <a href="https://github.com/Netflix/eureka/blob/master/eureka-client/src/main/java/com/netflix/appinfo/HealthCheckCallback.java">callback handler </a>
 * in <a href="https://github.com/Netflix/eureka/">eureka</a> to provide a
 * more flexible health check response (an HTTP status code) as a healthcheck request. <br/>
 *
 * This healthcheck handler is also used to have a fixed healthcheck endpoint created by karyon. <br/>
 *
 * By default, karyon uses this handler to also feed health status to eureka. If this is not desired, one should set a
 * dynamic property with name {@link PropertyNames#UNIFY_HEALTHCHECK_WITH_EUREKA} to <code>false</code>.
 *
 * @author Nitesh Kant
 */
public interface HealthCheckHandler {

    /**
     * Checks the health of the application and returns a status code, which can be directly consumed as a HTTP status
     * code. <br/>
     * <b>Kayon considers any status code &gt;= 200 and &lt; 300 as healthy.</b>
     *
     * @return The health status of the application.
     */
    int getStatus();
}
