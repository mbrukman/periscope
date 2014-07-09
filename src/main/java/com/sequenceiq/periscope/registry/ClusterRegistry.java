package com.sequenceiq.periscope.registry;

import java.util.Collection;

import com.sequenceiq.periscope.model.AmbariServer;

public interface ClusterRegistry {

    /**
     * Adds a new hadoop cluster to the registry.
     *
     * @param id           id of the cluster
     * @param ambariServer ambari server parameters
     */
    ClusterRegistration add(String id, AmbariServer ambariServer);

    /**
     * Removes a cluster from the registry.
     *
     * @param id id of the cluster
     */
    ClusterRegistration remove(String id);

    /**
     * Retrieves the registered cluster.
     *
     * @param id id of the cluster
     * @return cluster registration or null
     */
    ClusterRegistration get(String id);

    /**
     * Returns all the registered clusters.
     *
     * @return collection of clusters
     */
    Collection<ClusterRegistration> getAll();
}