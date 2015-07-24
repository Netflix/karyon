package com.netflix.karyon.healthcheck;

/**
 * Base definition for a specific HealthState (such as Healthy or Unhealthy).
 * 
 * @author elandau
 */
public interface HealthState {
    /**
     * A component may be healthy but in a degraded state.  Healthy here means that the component
     * is usable while unhealthy means that any call to the component will result in a failure.
     * 
     * @return The component is healthy and usable
     */
    boolean isUsable();
    
    /**
     * @return Printable health state name
     */
    String name();
    
    public enum States implements HealthState {
        /**
         * The component is healthy
         */
        HEALTHY(true),
        
        /**
         * The component is running in a recoverable degraded state but is otherwise healthy.
         * For example, a component may not have access to a remote IPC service but is able to 
         * serve a fallback
         */
        DEGRADED(true),
        
        /**
         * The component is still initializing and has never been healthy
         */
        STARTING(false),
        
        /**
         * The component is in a recoverable but unhealthy state
         */
        UNHEALTHY(false),
        
        /**
         * The component has been stopped and will never be healthy again
         */
        STOPPED(false),
        
        ;

        private boolean isHealthy;
        States(boolean isHealthy){
            this.isHealthy = isHealthy;
        }
        
        @Override
        public boolean isUsable() {
            return isHealthy;
        }
        
    }
}
