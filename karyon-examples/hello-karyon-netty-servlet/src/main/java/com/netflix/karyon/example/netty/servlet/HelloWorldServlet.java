package com.netflix.karyon.example.netty.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Nitesh Kant
 */
public class HelloWorldServlet extends HttpServlet {

    public HelloWorldServlet() {
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        if (session.isNew()) {
            session.setAttribute("XYZ", "xyz");
        } else {
            Object xyz = session.getAttribute("XYZ");
            System.out.println("xyz = " + xyz);
        }


        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("Hello ");
        String user = req.getParameter("name");
        if (null != user) {
            responseBuilder.append(user);
        }
        responseBuilder.append(" from karyon netty servlet example.");
        res.setStatus(200);
        PrintWriter writer = res.getWriter();
        writer.write(responseBuilder.toString());
        writer.flush();
        res.setHeader("X-From-Karyon", "true");
    }
}
