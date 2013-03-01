package com.netflix.adminresources;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A {@link javax.servlet.Servlet} to handle redirecting old links (currently from NAC) of the
 * form http://<hostname>:8077/AdminNetflixConfiguration/... - this servlet will
 * simply redirect to a new explorers based URL for the same functionality OR
 * emit the expected response itself. It will also handle redirecting "/" to
 * default view (baseserver unless overridden)
 * 
 * @author pkamath
 * @author Nitesh Kant
 */
@SuppressWarnings("serial")
public class RedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("/healthcheck");
    }
}
