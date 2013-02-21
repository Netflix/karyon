package com.netflix.karyon.spi;

/**
 * This is an extension to the <a href="https://github.com/Netflix/eureka/blob/master/eureka-client/src/main/java/com/netflix/appinfo/HealthCheckCallback.java">callback handler </a>
 * in <a href="https://github.com/Netflix/eureka/">eureka</a> to provide a
 * more flexible health check response (an HTTP status code) as a healthcheck request. <br/>
 * Any implementation of this handler, must be annotated as {@link com.netflix.governator.annotations.AutoBindSingleton}
 * so that karyon can find it during startup. <br/>
 *
 * In case, karyon does not find one, it will register a healthcheck callback which always returns 200 OK response with
 * eureka. <br/>
 *
 * This healthcheck handler is also used to have a fixed healthcheck endpoint created by karyon.
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
public interface HealthCheckHandler {

    /**
     * Checks the health of the application and returns a status code, which can be directly consumed as a HTTP status
     * code. <br/>
     * <b>Kayon considers any status code &gt;= 200 and &lt; 300 as healthy.</b>
     *
     * @return The health status of the application.
     */
    int checkHealth();
}
