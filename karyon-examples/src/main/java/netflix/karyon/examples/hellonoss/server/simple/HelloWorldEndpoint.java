package netflix.karyon.examples.hellonoss.server.simple;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.channel.StringTransformer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * @author Tomasz Bak
 */
public class HelloWorldEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldEndpoint.class);

    public Observable<Void> sayHello(HttpServerResponse<ByteBuf> response) {
        JSONObject content = new JSONObject();
        try {
            content.put("Message", "Hello from Netflix OSS");
            response.write(content.toString(), StringTransformer.DEFAULT_INSTANCE);
            return response.close();
        } catch (JSONException e) {
            logger.error("Error creating json response.", e);
            return Observable.error(e);
        }
    }

    public Observable<Void> sayHelloToUser(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        JSONObject content = new JSONObject();

        int prefixLength = "/hello/to".length();
        String userName = request.getPath().substring(prefixLength);

        try {
            if (userName.isEmpty() || userName.length() == 1 /*The uri is /hello/to/ but no name */) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
                content.put("Error", "Please provide a username to say hello. The URI should be /hello/to/{username}");
            } else {
                content.put("Message", "Hello " + userName.substring(1) /*Remove the / prefix*/ + " from Netflix OSS");
            }
        } catch (JSONException e) {
            logger.error("Error creating json response.", e);
            return Observable.error(e);
        }

        response.write(content.toString(), StringTransformer.DEFAULT_INSTANCE);
        return response.close();

    }
}
