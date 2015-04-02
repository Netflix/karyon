package netflix.karyon.jersey.blocking;

import com.google.inject.Injector;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.io.InputStream;

import netflix.karyon.transport.util.HttpContentInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class JerseyBasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(JerseyBasedRouter.class);

    private final ResourceConfig resourceConfig;
    private final Injector injector;
    private WebApplication application;
    private NettyToJerseyBridge nettyToJerseyBridge;

    public JerseyBasedRouter() {
        this(null);
    }

    @Inject
    public JerseyBasedRouter(Injector injector) {
        this.injector = injector;
        resourceConfig = new PropertiesBasedResourceConfig();
        ServiceIteratorProviderImpl.registerWithJersey();
    }

    @Override
    public Observable<Void> handle(final HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {

        /*
         * Creating the Container request eagerly, subscribes to the request content eagerly. Failure to do so, will
          * result in expiring/loss of content.
         */

        //we have to close input stream, to emulate normal lifecycle
        final InputStream requestData = new HttpContentInputStream( response.getAllocator(), request.getContent() );
        
        final ContainerRequest containerRequest = nettyToJerseyBridge.bridgeRequest( request, requestData );
        final ContainerResponseWriter containerResponse = nettyToJerseyBridge.bridgeResponse(response);

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    application.handleRequest(containerRequest, containerResponse);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    logger.error("Failed to handle request.", e);
                    subscriber.onError(e);
                }
                finally {
                  
                  //close input stream and release all data we buffered, ignore errors
                  try {
                    requestData.close();
                  }
                  catch( IOException e ) {
                  }
                }
            }
        }).doOnTerminate(new Action0() {
            @Override
            public void call() {
                response.close(true); /* Since this runs in a different thread, it needs an explicit flush,
                                         else the LastHttpContent will never be flushed and the client will not finish.*/
            }
        }).subscribeOn(Schedulers.io()) /*Since this blocks on subscription*/;
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
