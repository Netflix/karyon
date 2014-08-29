package com.netflix.karyon.ws.rs;

import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation of a RequestHandler that when given a RequestContext returns an 
 * Observable<Object> where a marshaller may be applied if not Observable<Void>.
 * Observable<Void> should be use in situations where the handler performs its
 * own marshalling. 
 * 
 * @author elandau
 *
 */
public interface RequestHandler extends Func1<RequestContext, Observable<Object>> {

}
