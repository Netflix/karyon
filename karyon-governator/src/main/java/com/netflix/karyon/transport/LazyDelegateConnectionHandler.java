package com.netflix.karyon.transport;

import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;

/**
* @author Tomasz Bak
*/
public class LazyDelegateConnectionHandler<I, O> implements ConnectionHandler<I, O> {
    private ConnectionHandler<I, O> connectionHandler;

    @Override
    public Observable<Void> handle(ObservableConnection<I, O> connection) {
        return connectionHandler.handle(connection);
    }

    public void setHandler(ConnectionHandler<I, O> connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
}
