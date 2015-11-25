package com.netflix.karyon.api;

import com.google.inject.Key;

/**
 * Default NoOp implementation of ProvisionMetrics.
 * 
 * @author elandau
 *
 */
final class NullProvisionMetrics implements ProvisionMetrics {
    @Override
    public void push(Key<?> key) {
    }

    @Override
    public void pop() {
    }

    @Override
    public void accept(Visitor visitor) {
    }
}
