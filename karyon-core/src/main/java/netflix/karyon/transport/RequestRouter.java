package netflix.karyon.transport;

import rx.Observable;

/**
 * @author Nitesh Kant
 *
 * @deprecated Use RxNetty's {@link io.reactivex.netty.channel.Handler} instead.
 */
@Deprecated
public interface RequestRouter<I, O> {

    Observable<Void> route(I request, O response);
}
