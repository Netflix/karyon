package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.netflix.karyon.server.http.spi.RequestContextAttributes.getOrCreateQueryStringDecoder;

/**
 * An implementation of {@link HttpRequestRouter} for routing to applications that follow the servlet model. <br/>
 *
 * @see com.netflix.karyon.server.http.servlet
 *
 * @author Nitesh Kant
 */
public class HttpServletRequestRouter implements HttpRequestRouter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestRouter.class);

    public static final int DEFAULT_SESSION_INACTIVE_PERIOD = (int) TimeUnit.SECONDS.convert(1, TimeUnit.DAYS);
    public static final String SESSION_INACTIVITY_SECONDS_PROP_NAME = "com.netflix.karyon.servlet.session.inactivity.period.seconds";
    private static final ErrorPageGenerator ERROR_PAGE_GENERATOR = new DefaultErrorPage();

    private final HttpSessionManager sessionManager;
    private final String contextPath;
    private final boolean secure;
    private final ServletsAndFiltersHolder holder;

    public HttpServletRequestRouter(String contextPath, boolean isSecure, ServletsAndFiltersHolder holder) {
        Preconditions.checkNotNull(contextPath, "Context path can not be null.");
        Preconditions.checkNotNull(holder, "Servlets & Filters holder can not be null.");
        this.contextPath = contextPath;
        secure = isSecure;
        this.holder = holder;
        int sessionInactivityPeriodSeconds =
                ConfigurationManager.getConfigInstance()
                                    .getInt(SESSION_INACTIVITY_SECONDS_PROP_NAME,
                                            DEFAULT_SESSION_INACTIVE_PERIOD);
        sessionManager = new HttpSessionManager(sessionInactivityPeriodSeconds);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void process(FullHttpRequest request, HttpResponseWriter responseWriter) {
        HttpServletRequestImpl servletRequest;
        HttpServletResponseImpl servletResponse;
        FilterChainImpl filterChain;
        try {
            ChannelHandlerContext channelHandlerContext = responseWriter.getChannelHandlerContext();

            ServletsAndFiltersHolder.ServletMatchResult servletMatchResult =
                                                        holder.getMatchingServlet(request, channelHandlerContext);
            if (null == servletMatchResult) {
                logger.warn("No matching servlet found for request URI: {}. Sending a 404 response.", request.getUri());
                responseWriter.createResponse(HttpResponseStatus.NOT_FOUND, null);
                responseWriter.sendResponse();
                return;
            }

            HttpServletRequestImpl.PathComponents pathComponents =
                    new HttpServletRequestImpl.PathComponents(getOrCreateQueryStringDecoder(request, channelHandlerContext),
                                                              contextPath, servletMatchResult.servletPath());
            servletRequest = new HttpServletRequestImpl(pathComponents, request, sessionManager,
                                                                               channelHandlerContext, secure);
            servletResponse = new HttpServletResponseImpl(responseWriter, ERROR_PAGE_GENERATOR, servletRequest);
            filterChain = new FilterChainImpl(servletMatchResult.servlet(),
                                              holder.getMatchingFilters(request, channelHandlerContext));
        } catch (Exception e) {
            sendErrorResponseBeforeFilterChainCreation(responseWriter, e);
            return;
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            sendErrorResponseOnFilterchainError(responseWriter, e);
        } finally {
            try {
                servletResponse.sendResponseIfNotSent();
            } catch (IOException e) {
                logger.error("Error while sending servlet response.", e);
            }
        }
    }

    private static void sendErrorResponseOnFilterchainError(HttpResponseWriter responseWriter,
                                                            Exception e) {
        logger.error("Error invoking the filter/servlet. Sending an error response.", e);
        FullHttpResponse response = responseWriter.response();
        if (response != null) {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendErrorResponseBeforeFilterChainCreation(HttpResponseWriter responseWriter,
                                                            Exception e) {
        logger.error("Error constructing the filter chain. Sending an error response.", e);
        ByteBuf errorPage = ERROR_PAGE_GENERATOR.getErrorPage(500, e.getMessage());
        if (!responseWriter.isResponseCreated()) {
            responseWriter.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, errorPage);
        }

        FullHttpResponse response = responseWriter.response();
        if (response != null) {
            response.content().writeBytes(errorPage);
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        responseWriter.sendResponse();
    }

    @Override
    public void start() {
    }

    @VisibleForTesting
    ServletsAndFiltersHolder servletsAndFiltersHolder() {
        return holder;
    }
}
