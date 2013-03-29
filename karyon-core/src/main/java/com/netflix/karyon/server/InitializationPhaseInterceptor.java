package com.netflix.karyon.server;

import java.util.EnumSet;

/**
 * This is an interceptor to tap into initialization phases of karyon. These lifecycle phases are much before governator
 * is started so we can not use governator's lifecycle callbacks. <br/>
 *
 * An interceptor can be registered using {@link PhaseInterceptorRegistry}
 *
 * @author Nitesh Kant
 */
public interface InitializationPhaseInterceptor {

    enum Phase {

        /**
         * This is the first thing that happens in the {@link KaryonServer}'s constructor.
         */
        OnCreate,

        /**
         * This is called before calling {@link com.netflix.karyon.server.ServerBootstrap#initialize()}.
         */
        InitBootstrap
    }

    /**
     * A callback when karyon enters the passed <code>phase</code>. <br/>
     * <b>All interceptors are invoked synchronously.</b>
     *
     * @param phase The new phase.
     */
    void onPhase(Phase phase);

    /**
     * A set of phases that this interceptor is interested in.
     *
     * @return Set of phases that this interceptor is interested in.
     */
    EnumSet<Phase> interestedIn();
}
