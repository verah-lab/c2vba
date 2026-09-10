package de.heuboe.wls.by.config;


/**
 * 
 * Return value true of isRunning() indicates that GeoManager is running (and is ready to respond to service calls) 
 * Used in Kubernetes cluster to provide 'running' state
 * ( A ReadyController bean on path '/ready' returns HTTP-OK when isRunning() == true, HTTP 503 otherwise ) 
 * 
 * @author peters
 *
 */
public interface SystemStateProvider {

    boolean isRunning();
}
