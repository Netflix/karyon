package netflix.karyon.server.interceptor;

import io.reactivex.netty.channel.Handler;
import rx.Observable;
import rx.functions.Action0;

/**
* @author Nitesh Kant
*/
class TestableRequestRouter<I, O> implements Handler<I, O> {

    private volatile boolean called;
    private volatile boolean unsubscribed;

    public boolean isReceivedACall() {
        return called;
    }

    public boolean isUnsubscribed() {
        return unsubscribed;
    }

    @Override
    public Observable<Void> handle(I input, O output) {
        called = true;
        return Observable.<Void>empty().doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                unsubscribed = true;
            }
        });
    }
}
