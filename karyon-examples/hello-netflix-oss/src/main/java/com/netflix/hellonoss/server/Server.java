package com.netflix.hellonoss.server;

import com.netflix.karyon.server.http.jersey.blocking.JerseyServer;
import com.netflix.karyon.server.http.servlet.blocking.HTTPServletRequestRouterBuilder;
import com.netflix.karyon.server.http.servlet.blocking.KaryonServlets;
import com.netflix.karyon.transport.http.HttpInterceptorSupport;
import io.netty.buffer.ByteBuf;

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

        JerseyServer.fromDefaults(8888).start();

        HTTPServletRequestRouterBuilder servletRouterBuilder = new HTTPServletRequestRouterBuilder()
                .forUri("/hello").serveWith(HelloWorldServlet.class);

        KaryonServlets.from(9999, servletRouterBuilder.build()).start();

        HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport = new HttpInterceptorSupport<ByteBuf, ByteBuf>();
        interceptorSupport.forUriRegex(".*").intercept(new LoggingInterceptor());

        KaryonServlets.from(8899, servletRouterBuilder.build(), interceptorSupport).startAndWait();
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
