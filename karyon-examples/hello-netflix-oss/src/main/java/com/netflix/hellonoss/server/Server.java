package com.netflix.hellonoss.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.netflix.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.server.http.jersey.blocking.JerseyServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.jersey.config.property.packages", "com.netflix"); // TODO: Karyon bootstrap
        JerseyServer.fromDefaults(8888, Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckHandler.class).to(HealthCheck.class);
            }
        })).startAndWait();
    }

    public static class HelloWorldServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.addHeader("X-Netflix-Hello", "Hello!!!");
            PrintWriter writer = resp.getWriter();
            writer.write("Hello from karyon servlets!");
            writer.flush();
        }
    }
}
