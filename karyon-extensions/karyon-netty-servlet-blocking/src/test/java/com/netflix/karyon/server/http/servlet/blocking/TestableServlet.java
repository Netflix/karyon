package com.netflix.karyon.server.http.servlet.blocking;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* @author Nitesh Kant
*/
public class TestableServlet extends HttpServlet {

    private static final long serialVersionUID = 1779057897162088887L;

    HttpServletRequest req;
    HttpServletResponse resp;
    volatile boolean invoked;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        invoked = true;
        this.req = req;
        this.resp = resp;
    }



}
