/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.karyon.spi;

import com.google.inject.ImplementedBy;

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
@ImplementedBy(DefaultHealthCheckHandler.class)
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
