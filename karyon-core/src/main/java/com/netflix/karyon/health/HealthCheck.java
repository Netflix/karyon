package com.netflix.karyon.health;

/**
 * SPI for a component implementation an application status condition.  
 * Status conditions can indicate one of three scenarios
 * 1.  Component is not ready (i.e. starting or initializing)
 * 2.  Component is ready and functioning propertly
 * 3.  Component has either failed to start or failed during runtime
 * 
 * There can be multiple status conditions registered for a single application.  
 * The status conditions are ultimately resolved to a single application up/down
 * status.
 * 
 * @author elandau
 */
public interface HealthCheck {
    /**
     * The status can have one of three states
     * 
     * 1.  Not ready    status = false, error = null
     * 2.  Ready        status = true,  error = null
     * 3.  Error state  status = false, error ! =null
     * 
     * @author elandau
     *
     */
    public static class Status {
        private final HealthCheck healthCheck;
        private final Throwable error;
        private final boolean status;
        
        private Status(HealthCheck healthCheck, boolean status, Throwable error) {
            this.status = status;
            this.error = error;
            this.healthCheck = healthCheck;
        }
        
        /**
         * Component is ready to be used and functioning properly
         * @return
         */
        public boolean isReady() {
            return status && error == null;
        }
        
        /**
         * Component is either not ready or failed
         */
        public boolean isNotReady() {
            return !isReady();
        }
        
        /**
         * There was an error starting or running a component
         */
        public boolean hasError() {
            return error != null;
        }
        
        public Throwable getError() {
            return error;
        }
        
        /**
         * @return Name of component(s) being checked by this StatusCheck
         */
        public String getName() {
            return healthCheck.getName();
        }

        public static Status error(HealthCheck healthCheck, Throwable error) {
            return new Status(healthCheck, false, error);
        }
        
        public static Status ready(HealthCheck healthCheck) {
            return new Status(healthCheck, true, null);
        }
        
        public static Status notReady(HealthCheck healthCheck) {
            return new Status(healthCheck, false, null);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Status [")
              .append("healthCheck=").append(healthCheck.getName())
              .append(", status=")   .append(status);
            
            if (error != null) {
                sb.append(", error=" + error.getMessage());
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Run the status check and return the current status
     * @return 
     */
    public abstract Status check();

    /**
     * @return Get name for health check
     */
    public abstract String getName();
}
