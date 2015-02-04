package netflix.karyon;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.server.RxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * A shutdown listener for karyon which aids shutdown of a server using a remote command over a socket.
 *
 * @author Nitesh Kant
 */
public class ShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);

    private final RxServer<String, String> shutdownCmdServer;

    public ShutdownListener(int shutdownPort, final Func1<String, Observable<Void>> commandHandler) {
        shutdownCmdServer = RxNetty.createTcpServer(shutdownPort,
                                         PipelineConfigurators.stringMessageConfigurator(),
                                         new ShutdownConnectionHandler(commandHandler));
    }

    public ShutdownListener(int shutdownPort, final Action0 shutdownAction) {
        this(shutdownPort, new DefaultCommandHandler(shutdownAction));
    }

    public int getShutdownPort() {
        return shutdownCmdServer.getServerPort();
    }

    public void start() {
        shutdownCmdServer.start();
    }

    public void shutdown() throws InterruptedException {
        shutdownCmdServer.shutdown();
    }

    private static class DefaultCommandHandler implements Func1<String, Observable<Void>> {

        private final ExecutorService shutdownExec;
        private final Action0 shutdownAction;

        public DefaultCommandHandler(Action0 shutdownAction) {
            this.shutdownAction = shutdownAction;
            shutdownExec = Executors.newFixedThreadPool(1);
        }

        @Override
        public Observable<Void> call(String cmd) {
            if ("shutdown".equalsIgnoreCase(cmd)) {
                return shutdownAsync();
            }
            return Observable.error(new UnsupportedOperationException("Unknown command: " + cmd));
        }

        private Observable<Void> shutdownAsync() {
            final Future<Void> submitFuture = shutdownExec.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    shutdownAction.call();
                    return null;
                }
            });
            return Observable.interval(10, TimeUnit.SECONDS)
                             .take(1)
                             .map(new Func1<Long, Void>() {
                                 @Override
                                 public Void call(Long aLong) {
                                     logger.info("Checking if shutdown is done..");
                                     if (submitFuture.isDone()) {
                                         try {
                                             submitFuture.get();
                                             logger.info("Shutdown is done..");
                                         } catch (InterruptedException e) {
                                             logger.info("Shutdown returned error. ", e);
                                         } catch (ExecutionException e) {
                                             if (e.getCause() instanceof IllegalStateException) {
                                                 logger.info("Server already shutdown. ", e);
                                             } else {
                                                 logger.info("Shutdown returned error. ", e);
                                             }
                                         }
                                     } else {
                                         logger.debug("Shutdown not yet done.");
                                     }
                                     return null;
                                 }
                             });
        }
    }

    private class ShutdownConnectionHandler implements ConnectionHandler<String, String> {
        private final Func1<String, Observable<Void>> commandHandler;

        public ShutdownConnectionHandler(Func1<String, Observable<Void>> commandHandler) {
            this.commandHandler = commandHandler;
        }

        @Override
        public Observable<Void> handle(final ObservableConnection<String, String> conn) {
            return conn.getInput().take(1) /*Take only one command per connection*/
                       .doOnNext(new Action1<String>() {
                           @Override
                           public void call(String s) {
                               logger.info("Received a command: " + s);
                           }
                       })
                       .flatMap(commandHandler)
                       .doOnCompleted(new Action0() {
                           @Override
                           public void call() {
                               try {
                                   shutdown();
                               } catch (InterruptedException e) {
                                   logger.error("Interrupted while shutting down the shutdown command listener.");
                               }
                           }
                       });
        }
    }
}
