package com.sequenceiq.periscope.policies.cloudbreak.rule;

import java.util.Map;

import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;

import com.sequenceiq.periscope.policies.NamedRule;

public interface ClusterAdjustmentRule extends NamedRule {

    /**
     * Invoked when creating the rule.
     *
     * @param config config parameters
     */
    void init(Map<String, Object> config);

    /**
     * Scale up or down to the required number of nodes.
     *
     * @param clusterInfo
     * @return
     */
    int scale(ClusterMetricsInfo clusterInfo);

    /**
     * Return the maximum/minimum number of nodes. Depends whether its scaling up or down.
     */
    int getLimit();
}
