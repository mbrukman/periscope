package com.sequenceiq.periscope.policies.scaling.rule.scaleup;

import static java.lang.Math.ceil;
import static java.lang.Math.max;

import java.util.Map;

import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;

import com.sequenceiq.periscope.policies.scaling.rule.AbstractScalingRule;
import com.sequenceiq.periscope.policies.scaling.rule.ScalingRule;

public class PendingContainersRule extends AbstractScalingRule implements ScalingRule {

    public static final String NAME = "pendingContainers";
    private int pendingContainersLimit;

    @Override
    public void init(Map<String, String> config) {
        setName(NAME);
        setLimit(Integer.valueOf(config.get("limit")));
        this.pendingContainersLimit = Integer.valueOf(config.get("pendingContainers"));
    }

    @Override
    public int scale(ClusterMetricsInfo clusterInfo) {
        int pendingContainers = clusterInfo.getPendingContainers();
        if (isPendingContainersExceed(pendingContainers)) {
            int containerPerNode = calcAvgContainerPerNode(clusterInfo);
            return max(getLimit(), (int) ceil((double) pendingContainers / containerPerNode));
        }
        return 0;
    }

    private boolean isPendingContainersExceed(int pendingContainers) {
        return pendingContainers > pendingContainersLimit;
    }

    private int calcAvgContainerPerNode(ClusterMetricsInfo clusterInfo) {
        int allocated = clusterInfo.getContainersAllocated();
        int activeNodes = clusterInfo.getActiveNodes();
        return (int) ceil((double) allocated / activeNodes);
    }

}