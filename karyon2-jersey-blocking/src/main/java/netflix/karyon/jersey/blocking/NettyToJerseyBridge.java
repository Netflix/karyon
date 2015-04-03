package netflix.karyon.jersey.blocking;

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.server.HttpResponseHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author Nitesh Kant
 */
final class NettyToJerseyBridge {

    private static final Logger logger = LoggerFactory.getLogger(NettyToJerseyBridge.class);

    private final WebApplication application;

    NettyToJerseyBridge(WebApplication application) {
        this.application = application;
    }

    ContainerRequest bridgeRequest(final HttpServerRequest<ByteBuf> nettyRequest, InputStream requestData ) {
        try {
            URI baseUri = new URI("/"); // Since the netty server does not have a context path element as such, so base uri is always /
            URI uri = new URI(nettyRequest.getUri());
            return new ContainerRequest(application, nettyRequest.getHttpMethod().name(),
                                        baseUri, uri, new JerseyRequestHeadersAdapter(nettyRequest.getHeaders()),
                                        requestData );
        } catch (URISyntaxException e) {
            logger.error(String.format("Invalid request uri: %s", nettyRequest.getUri()), e);
            throw new IllegalArgumentException(e);
        }
    }

    ContainerResponseWriter bridgeResponse(final HttpServerResponse<ByteBuf> serverResponse) {
        return new ContainerResponseWriter() {

            private final ByteBuf contentBuffer = serverResponse.getChannel().alloc().buffer();

            @Override
            public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) {
                int responseStatus = response.getStatus();
                serverResponse.setStatus(HttpResponseStatus.valueOf(responseStatus));
                HttpResponseHeaders responseHeaders = serverResponse.getHeaders();
                for(Map.Entry<String, List<Object>> header : response.getHttpHeaders().entrySet()){
                    responseHeaders.setHeader(header.getKey(), header.getValue());
                }
                return new ByteBufOutputStream(contentBuffer);
            }

            @Override
            public void finish() {
                serverResponse.writeAndFlush(contentBuffer);
            }
        };
    }

    private static class JerseyRequestHeadersAdapter extends InBoundHeaders {

        private static final long serialVersionUID = 2303297923762115950L;

        private final HttpRequestHeaders requestHeaders;
        private Set<Map.Entry<String, List<String>>> entrySet;
        private Collection<List<String>> values;

        private JerseyRequestHeadersAdapter(HttpRequestHeaders requestHeaders) {
            this.requestHeaders = requestHeaders;
        }

        @Override
        public void putSingleObject(String key, Object value) {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        public void addObject(String key, Object value) {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        public <A> List<A> get(String key, Class<A> type) {
            if (!type.isAssignableFrom(String.class)) {
                return Collections.emptyList();
            }
            @SuppressWarnings("unchecked")
            List<A> values = (List<A>) requestHeaders.getAll(key);
            return values;
        }

        @Override
        public <A> A getFirst(String key, Class<A> type) {
            List<A> values = get(key, type);
            return null != values && !values.isEmpty() ? values.get(0) : null;
        }

        @Override
        public <A> A getFirst(String key, A defaultValue) {
            @SuppressWarnings("unchecked")
            A value = (A) getFirst(key, defaultValue.getClass());
            return null != value ? value : defaultValue;
        }

        @Override
        public void putSingle(String key, String value) {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        public void add(String key, String value) {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        public String getFirst(String key) {
            return getFirst(key, String.class);
        }

        @Override
        protected List<String> getList(String key) {
            return get(key, String.class);
        }

        @Override
        public boolean containsValue(Object value) {
            List<Map.Entry<String, String>> entries = requestHeaders.entries();
            for (Map.Entry<String, String> entry : entries) {
                if (value.equals(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<String> get(Object key) {
            return getList(String.valueOf(key));
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
            throw new UnsupportedOperationException("No modifications allowed on request headers."); // The API is sad
        }

        @Override
        public int size() {
            return requestHeaders.names().size();
        }

        @Override
        public boolean isEmpty() {
            return requestHeaders.names().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return requestHeaders.contains(String.valueOf(key));
        }

        @Override
        public List<String> put(String key, List<String> value) {
            throw new UnsupportedOperationException("No modifications allowed on request headers.");
        }

        @Override
        public void putAll(Map<? extends String, ? extends List<String>> m) {
            throw new UnsupportedOperationException("No modifications allowed on request headers.");
        }

        @Override
        public List<String> remove(Object key) {
            throw new UnsupportedOperationException("No modifications allowed on request headers.");
        }

        @Override
        public synchronized Set<Map.Entry<String, List<String>>> entrySet() {
            if (null != entrySet) {
                return entrySet;
            }
            List<Map.Entry<String, String>> entries = requestHeaders.entries();
            entrySet = new HashSet<Map.Entry<String, List<String>>>(entries.size());
            for (final Map.Entry<String, String> entry : entries) {
                ArrayList<String> listValue = new ArrayList<String>();
                listValue.add(entry.getValue());
                entrySet.add(new SimpleEntry<String, List<String>>(entry.getKey(), listValue));
            }
            return entrySet;
        }

        @Override
        public Set<String> keySet() {
            return requestHeaders.names();
        }

        @Override
        public synchronized Collection<List<String>> values() {
            if (null != values) {
                return values;
            }

            values = new ArrayList<List<String>>();
            for (String headerName : requestHeaders.names()) {
                values.add(requestHeaders.getAll(headerName));
            }
            return values;
        }

        @Override
        public boolean equals(Object o) {
            return requestHeaders.equals(o);
        }

        @Override
        public int hashCode() {
            return requestHeaders.hashCode();
        }

        @Override
        public String toString() {
            return requestHeaders.toString();
        }
    }

}
