package com.netflix.karyon.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.Karyon;

public class ServletContextListenerTest {
    private static String RESPONSE = "Hello World!";
    
    @Singleton
    public static class MyServlet extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            resp.setStatus(200);
            resp.getOutputStream().write(RESPONSE.getBytes());
            resp.getOutputStream().close();
        }
    }
    
    public static class TestServletContextListener extends KaryonServletContextListener {
        @Override
        protected LifecycleInjector createInjector() throws Exception {
            return Karyon
                .bootstrap()
                .addModules(
                        new ServletModule() {
                            @Override
                            protected void configureServlets() {
                                serve("/").with(MyServlet.class);
                            }
                        })
                .start();
        }
    }

    public static class FailingServletContextListener extends KaryonServletContextListener {
        @Override
        protected LifecycleInjector createInjector() throws Exception {
            throw new Exception("Failed");
        }
    }
    
    @Test
    public void testServletContextListener() throws Exception {
        Server server = new Server(0);
        WebAppContext webapp = new WebAppContext();
        webapp.setResourceBase("src/main/webapp");
        webapp.addEventListener(new TestServletContextListener());
        webapp.setServer(server);
        webapp.addFilter(GuiceFilter.class, "/*", null);
        webapp.addServlet(DefaultServlet.class,"/");
        server.setHandler(webapp);
        server.start();
        
        Assert.assertTrue(webapp.getServletContext().isEnabled());
        
        int port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        try (BufferedReader is = new BufferedReader(new InputStreamReader(new URL("http://localhost:" + port + "/").openConnection().getInputStream()))) {
            String response = is.readLine();
            Assert.assertEquals(RESPONSE, response);
        }
        
        server.stop();
    }
    
    @Test
    public void testFailingServletContextListener() throws Exception {
        Server server = new Server(0);
        WebAppContext webapp = new WebAppContext();
        webapp.setResourceBase("src/main/webapp");
        webapp.addEventListener(new FailingServletContextListener());
        webapp.setServer(server);
        webapp.addFilter(GuiceFilter.class, "/*", null);
        webapp.addServlet(DefaultServlet.class,"/");
        server.setHandler(webapp);
        server.start();
        
        Assert.assertTrue(webapp.getServletContext().isEnabled());
        
        int port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        
        HttpURLConnection conn = (HttpURLConnection)new URL("http://localhost:" + port + "/").openConnection();
        conn.connect();
        Assert.assertEquals(503, conn.getResponseCode());
        
        server.stop();        
    }
}
