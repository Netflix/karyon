package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.transport.http.HttpKeyEvaluationContext;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import com.netflix.karyon.transport.http.QueryStringDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;


/**
 * An implementation of {@link HttpRequestRouter} for routing to applications that follow the servlet model. <br/>
 *
 * @see com.netflix.karyon.server.http.servlet
 *
 * @author Nitesh Kant
 */
public class HttpServletRequestRouter implements HttpRequestRouter<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestRouter.class);

    public static final int DEFAULT_SESSION_INACTIVE_PERIOD = (int) TimeUnit.SECONDS.convert(1, TimeUnit.DAYS);
    public static final String SESSION_INACTIVITY_SECONDS_PROP_NAME = "com.netflix.karyon.servlet.session.inactivity.period.seconds";

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
    public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        HttpServletRequestImpl servletRequest;
        HttpServletResponseImpl servletResponse;
        FilterChainImpl filterChain;
        try {
            ChannelHandlerContext channelHandlerContext = response.getChannelHandlerContext();

            ServletsAndFiltersHolder.ServletMatchResult servletMatchResult =
                    holder.getMatchingServlet(request, channelHandlerContext);
            if (null == servletMatchResult) {
                logger.warn("No matching servlet found for request URI: {}. Sending a 404 response.", request.getUri());
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return response.close();
            }

            QueryStringDecoder queryStringDecoder =
                    HttpKeyEvaluationContext.getOrCreateQueryStringDecoder(request, channelHandlerContext);
            HttpServletRequestImpl.PathComponents pathComponents =
                    new HttpServletRequestImpl.PathComponents(queryStringDecoder,
                                                              contextPath, servletMatchResult.servletPath());
            servletRequest = new HttpServletRequestImpl(pathComponents, request, sessionManager,
                                                        channelHandlerContext, secure);
            servletResponse = new HttpServletResponseImpl(response, servletRequest);
            filterChain = new FilterChainImpl(servletMatchResult.servlet(),
                                              holder.getMatchingFilters(request, channelHandlerContext));
        } catch (Exception e) {
            logger.error("Error constructing the filter chain. Sending an error response.", e);
            return Observable.error(e);
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            logger.error("Error invoking the filter/servlet. Sending an error response.", e);
            return Observable.error(e);
        }

        return Observable.empty(); // Since execution is blocking, if this stmt is reached, it means execution is over.
    }

    @VisibleForTesting
    ServletsAndFiltersHolder servletsAndFiltersHolder() {
        return holder;
    }
}
