package netflix.karyon.transport.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.interceptor.InterceptorKey;
import rx.Observable;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple implementation for handling requests for different URIs. This router provides a facility to update the
 * routes at runtime. Thread-safety is maintained by using a "copy-on-write" data structure underneath, hence it comes
 * with an object allocation cost.
 *
 * The correct way to use this router would be to create all the routes at startup and then do not update the routes
 * at runtime unless it is required.
 *
 * @author Nitesh Kant
 */
public class SimpleUriRouter<I, O> implements RequestHandler<I, O> {

    private final CopyOnWriteArrayList<Route> routes;

    public SimpleUriRouter() {
        routes = new CopyOnWriteArrayList<Route>();
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(response.getChannel());
        for (Route route : routes) {
            if (route.key.apply(request, context)) {
                return route.getHandler().handle(request, response);
            }
        }

        // None of the routes matched.
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        return response.close();
    }

    /**
     * Add a new URI -&lt; Handler route to this router.
     * @param uri URI to match.
     * @param handler Request handler.
     * @return The updated router.
     */
    public SimpleUriRouter<I, O> addUri(String uri, RequestHandler<I, O> handler) {
        routes.add(new Route(new ServletStyleUriConstraintKey<I>(uri, ""), handler));
        return this;
    }

    /**
     * Add a new URI regex -&lt; Handler route to this router.
     * @param uriRegEx URI regex to match
     * @param handler Request handler.
     * @return The updated router.
     */
    public SimpleUriRouter<I, O> addUriRegex(String uriRegEx, RequestHandler<I, O> handler) {
        routes.add(new Route(new RegexUriConstraintKey<I>(uriRegEx), handler));
        return this;
    }

    private class Route {

        private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
        private final RequestHandler<I, O> handler;

        public Route(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
                      RequestHandler<I, O> handler) {
            this.key = key;
            this.handler = handler;
        }

        public InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> getKey() {
            return key;
        }

        public RequestHandler<I, O> getHandler() {
            return handler;
        }
    }
}
