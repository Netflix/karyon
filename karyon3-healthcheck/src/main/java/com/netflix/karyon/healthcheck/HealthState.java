package com.netflix.karyon.healthcheck;

/**
 * Base definition for a specific HealthState (such as Healthy or Unhealthy).
 * 
 * @author elandau
 */
public interface HealthState {
    /**
     * Indicates that a component in this state is usable.  
     * 
     * @return The component is usable
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

        private boolean isUsable;
        States(boolean isUsable){
            this.isUsable = isUsable;
        }
        
        @Override
        public boolean isUsable() {
            return isUsable;
        }
        
    }
}
