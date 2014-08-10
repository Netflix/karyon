package com.netflix.karyon.jersey.blocking;

import com.google.inject.Injector;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Nitesh Kant
 */
public class JerseyBasedRouter implements HttpRequestRouter<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(JerseyBasedRouter.class);

    private final ResourceConfig resourceConfig;
    private final Injector injector;
    private final Pattern staticContentPattern;
    private WebApplication application;
    private NettyToJerseyBridge nettyToJerseyBridge;
    private HttpStaticRequestRouter staticRequestRouter = new HttpStaticRequestRouter();
    
    public JerseyBasedRouter() {
        this(null);
    }

    @Inject
    public JerseyBasedRouter(Injector injector) {
        this.injector = injector;
        resourceConfig = new PropertiesBasedResourceConfig();
        String regex = (String)resourceConfig.getProperty(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX);
        if (regex != null && regex.length() > 0) {
            try {
                staticContentPattern = Pattern.compile(regex);
            } catch (PatternSyntaxException ex) {
                throw new ContainerException(
                        
                        "The syntax is invalid for the regular expression, " + regex +
                                ", associated with the initialization parameter " + ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX, ex);
            }
        }
        else {
            staticContentPattern = null;
        }

        ServiceIteratorProviderImpl.registerWithJersey();
    }

    @Override
    public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        try {
            if (this.staticContentPattern != null && this.staticContentPattern.matcher(request.getUri()).matches()) {
                return staticRequestRouter.route(request, response);
            }

            application.handleRequest(nettyToJerseyBridge.bridgeRequest(request, response.getAllocator()),
                                      nettyToJerseyBridge.bridgeResponse(response));
        } catch (IOException e) {
            logger.error("Failed to handle request.", e);
            return Observable.error(e);
        }

        return Observable.empty(); // Since execution is blocking, if this stmt is reached, it means execution is over.
    }

    @PostConstruct
    public void start() {
        NettyContainer container;
        if (null != injector) {
            container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig,
                                                         new GuiceComponentProviderFactory(resourceConfig, injector));
        } else {
            container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig);
        }
        application = container.getApplication();
        nettyToJerseyBridge = container.getNettyToJerseyBridge();
        logger.info("Started Jersey based request router.");
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopped Jersey based request router.");
        application.destroy();
    }
}
