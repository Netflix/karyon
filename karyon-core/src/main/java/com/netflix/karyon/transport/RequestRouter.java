package com.netflix.karyon.transport;

import rx.Observable;

/**
 * @author Nitesh Kant
 */
public interface RequestRouter<I, O> {

    Observable<Void> route(I request, O response);
}
