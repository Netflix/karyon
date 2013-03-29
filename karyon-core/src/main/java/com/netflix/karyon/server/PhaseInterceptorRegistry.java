package com.netflix.karyon.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.netflix.karyon.server.utils.KaryonUtils;
import com.netflix.karyon.spi.PropertyNames;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import static com.netflix.karyon.server.InitializationPhaseInterceptor.Phase;

/**
 * A static registry of {@link InitializationPhaseInterceptor}s. Since, karyon initialization phases occur once in the
 * server's lifetime, the registry if changed after the phase has passed, does not fetch anything useful. However, the
 * registry does not check for validity of such a change.
 *
 * @author Nitesh Kant
 */
public class PhaseInterceptorRegistry {

    private static SetMultimap<Phase, InitializationPhaseInterceptor> phaseVsInterceptors =
            Multimaps.newSetMultimap(new EnumMap<Phase, Collection<InitializationPhaseInterceptor>>(Phase.class),
                                     new Supplier<Set<InitializationPhaseInterceptor>>() {
                                         @Override
                                         public Set<InitializationPhaseInterceptor> get() {
                                             return new HashSet<InitializationPhaseInterceptor>();
                                         }
                                     });

    static {
        if (!Boolean.getBoolean(PropertyNames.DISABLE_ARCHAIUS_INTEGRATION)) {
            register(new ArchaiusIntegrationInterceptor());
        }
    }

    /**
     * Registers a new interceptor.
     *
     * @param interceptor Interceptor to register.
     *
     * @return <code>true</code> if the interceptor was registered for atleast one of the interested phases,
     * <code>false</code> if this interceptor was already registered for all the phases.
     *
     * @throws NullPointerException If the interceptor is null.
     */
    public static boolean register(InitializationPhaseInterceptor interceptor) {

        Preconditions.checkNotNull(interceptor, "Interceptor to be registered can not be null.");
        Preconditions.checkNotNull(interceptor.interestedIn(), "Interceptor's interest set can not be null.");

        boolean success = false;
        for (Phase phase : interceptor.interestedIn()) {
            if (phaseVsInterceptors.put(phase, interceptor)) {
                success = true; // atleast one success
            }
        }

        return success;
    }

    /**
     * Returns a set of interceptors for the passed phase. This set is backed by the actual underlying set of
     * interceptors so any change to the set will result in change to this registry contents.
     *
     * @param phase Phase for which the interceptors are to be returned.
     *
     * @return Set of interceptors. Empty set if none. This set is backed by the actual underlying set of
     * interceptors so any change to the set will result in change to this registry contents.
     */
    public static Set<InitializationPhaseInterceptor> getInterceptors(Phase phase) {
        return phaseVsInterceptors.get(phase);
    }

    /**
     * Notifies all interceptors for the passed <code>phase</code>
     *
     * @param phase Phase entered by karyon.
     */
    static void notifyInterceptors(Phase phase) {
        for (InitializationPhaseInterceptor interceptor : getInterceptors(phase)) {
            interceptor.onPhase(phase);
        }
    }
}
