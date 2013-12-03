/**
 * This package intends to act as a way to run servlet based applications inside an embedded netty container. <br/>
 *
 * <h2>What does it do?</h2>
 *<ul>
 <li>Implement the runtime aspects of the servlet specification. This means:
 <ul>
 <li>You can configure which servlets to run for which kind of requests.</li>
 <li>You can configure servlet filters to run for which kind of requests.</li>
 </ul></li>
 </ul>
 *
 * <h2>What does it NOT do?</h2>
 *<ul>
 <li>Does NOT implement the lifecycle aspects of the servlet specification. This means:
 <ul>
 <li>You can not deploy an application war file.</li>
 <li>There is no "deployment" per se of any code. All servlets/filters must be configured programmatically using
 {@link HttpServletRequestRouter}</li>
 <li>You will not get lifecycle callbacks eg: {@link ServletContextListener#contextInitialized(ServletContextEvent)}</li>
 <li>Does not implement servlet security.</li>
 <li>Does not implement session lifecycle callbacks.</li>
 <li>Does not offer a per session maximum inactive time before expiry. It only offers a global inactive time for all sessions.</li>
 </ul>
 </li>
 </ul>
 */
package com.netflix.karyon.server.http.servlet.blocking;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;