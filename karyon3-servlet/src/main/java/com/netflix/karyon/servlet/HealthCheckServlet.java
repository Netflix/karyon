package com.netflix.karyon.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.karyon.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckStatus;

@Singleton
public class HealthCheckServlet extends HttpServlet {

    private final HealthCheck healthCheck;
    private final ObjectMapper mapper;

    @Inject
    public HealthCheckServlet(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
        this.mapper = new ObjectMapper();
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String content;
        try {
            HealthCheckStatus status = healthCheck.check().get();
            content = mapper.writeValueAsString(status.getIndicators());
            switch (status.getState()) {
            case Starting:
                response.setStatus(204);
                break;
            case Healthy:
                response.setStatus(200);
                break;
            case Unhealthy:
                response.setStatus(500);
                break;
            case OutOfService:
                response.setStatus(500);
                break;
            }
        } catch (InterruptedException | ExecutionException e) {
            response.setStatus(500);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            content = sw.toString();
        }
        
        response.getWriter().print(content);
        response.setContentLength(content.length());
        response.setContentType("text/plain");
    }
}
