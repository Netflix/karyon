package netflix.adminresources;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            String url = ((HttpServletRequest)request).getRequestURI();
            if (url.startsWith("/main") || url.startsWith("/auth")) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
