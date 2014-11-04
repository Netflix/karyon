package netflix.karyon.jersey.blocking;

import com.sun.jersey.spi.container.WebApplication;

/**
 * @author Nitesh Kant
 */
public class NettyContainer {

    private final WebApplication application;
    private final NettyToJerseyBridge nettyToJerseyBridge;

    public NettyContainer(WebApplication application) {
        this.application = application;
        nettyToJerseyBridge = new NettyToJerseyBridge(application);
    }

    NettyToJerseyBridge getNettyToJerseyBridge() {
        return nettyToJerseyBridge;
    }

    WebApplication getApplication() {
        return application;
    }
}
