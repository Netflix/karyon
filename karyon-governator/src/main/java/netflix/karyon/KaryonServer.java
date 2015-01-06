package netflix.karyon;

/**
 * A logical abstraction to manage the lifecycle of a karyon based application.
 * This does not define any contracts of handling and processing requests, those should all be defined by means of
 * modules.
 */
public interface KaryonServer {

    /**
     * Starts the server and hence the modules associated with this server.
     */
    void start();

    /**
     * Shutdown the server and hence the modules associated with this server.
     */
    void shutdown();

    /**
     * A utility method to block the caller thread till the server is shutdown (by external invocation).
     * <b>This method does not start or shutdown the server. It just waits for shutdown.</b>
     */
    void waitTillShutdown();

    /**
     * A shorthand for calling {@link #start()} and {@link #waitTillShutdown()}
     */
    void startAndWaitTillShutdown();
}
