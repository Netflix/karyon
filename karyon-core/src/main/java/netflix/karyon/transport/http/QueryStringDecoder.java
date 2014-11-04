package netflix.karyon.transport.http;

/**
 * A simple wrapper over {@link io.netty.handler.codec.http.QueryStringDecoder} to also provide access to the uri string.
 *
 * @author Nitesh Kant
 */
public class QueryStringDecoder {

    private final io.netty.handler.codec.http.QueryStringDecoder nettyDecoder;
    private final String uri;

    public QueryStringDecoder(String uri) {
        if (null == uri) {
            throw new NullPointerException("Uri can not be null.");
        }
        this.uri = uri;
        uri = io.netty.handler.codec.http.QueryStringDecoder.decodeComponent(uri);
        if (!uri.endsWith("/") && !uri.contains(".") && !uri.contains("?")) {
            // Normalize the URI for better matching of Servlet style URI constraints.
            uri += "/";
        }
        nettyDecoder = new io.netty.handler.codec.http.QueryStringDecoder(uri);
    }

    public io.netty.handler.codec.http.QueryStringDecoder nettyDecoder() {
        return nettyDecoder;
    }

    public String uri() {
        return uri;
    }
}
