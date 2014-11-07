package netflix.karyon.examples.tcp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;
import rx.functions.Func1;

import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Tomasz Bak
 */
public interface TcpPipelineHandlers {

    @Singleton
    class QueueProvider {
        private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        public boolean isEmpty() {
            return messageQueue.isEmpty();
        }

        public String poll() {
            return messageQueue.poll();
        }

        public void put(String message) {
            messageQueue.add(message);
        }
    }

    class FrontendConnectionHandler implements ConnectionHandler<ByteBuf, ByteBuf> {
        private final QueueProvider queueProvider;

        @Inject
        public FrontendConnectionHandler(QueueProvider queueProvider) {
            this.queueProvider = queueProvider;
        }

        @Override
        public Observable<Void> handle(final ObservableConnection<ByteBuf, ByteBuf> connection) {
            System.out.println("New frontend connection");
            return connection.getInput().flatMap(new Func1<ByteBuf, Observable<Void>>() {
                @Override
                public Observable<Void> call(ByteBuf byteBuf) {
                    String message = byteBuf.toString(Charset.defaultCharset());
                    System.out.println("Received: " + message);
                    queueProvider.put(message);

                    ByteBuf output = connection.getAllocator().buffer();
                    output.writeBytes("Want some more:\n".getBytes());
                    return connection.writeAndFlush(output);
                }
            });
        }
    }

    class BackendConnectionHandler implements ConnectionHandler<ByteBuf, ByteBuf> {

        private final QueueProvider queueProvider;

        @Inject
        public BackendConnectionHandler(QueueProvider queueProvider) {
            this.queueProvider = queueProvider;
        }

        @Override
        public Observable<Void> handle(final ObservableConnection<ByteBuf, ByteBuf> connection) {
            System.out.println("New backend connection");

            return Observable.interval(1, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Void>>() {
                @Override
                public Observable<Void> call(Long tick) {
                    while (!queueProvider.isEmpty()) {
                        ByteBuf output = connection.getAllocator().buffer();
                        output.writeBytes(queueProvider.poll().getBytes());
                        connection.write(output);
                    }
                    return connection.flush();
                }
            });
        }
    }
}
