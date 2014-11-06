package netflix.karyon.server.interceptor;

import io.reactivex.netty.channel.Handler;
import rx.Observable;

/**
* @author Nitesh Kant
*/
class TestableRequestRouter<I, O> implements Handler<I, O> {

    private volatile boolean called;

    public boolean isReceivedACall() {
        return called;
    }

    @Override
    public Observable<Void> handle(I input, O output) {
        called = true;
        return Observable.empty();
    }
}
