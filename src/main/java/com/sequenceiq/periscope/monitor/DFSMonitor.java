package com.sequenceiq.periscope.monitor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.monitor.request.DFSMetricRequest;
import com.sequenceiq.periscope.monitor.request.RequestContext;
import com.sequenceiq.periscope.service.configuration.ConfigParam;

@Component
public class DFSMonitor extends AbstractMonitor implements Monitor {

    @Override
    public String getIdentifier() {
        return "dfs-monitor";
    }

    @Override
    public String getTriggerExpression() {
        return MonitorUpdateRate.DFS_UPDATE_RATE_CRON;
    }

    @Override
    public Class getRequestType() {
        return DFSMetricRequest.class;
    }

    @Override
    public Map<String, Object> getRequestContext(Cluster cluster) {
        Map<String, Object> context = new HashMap<>();
        context.put(RequestContext.CLUSTER_ID.name(), cluster.getId());
        context.put(RequestContext.NAMENODE_WEB_ADDRESS.name(), cluster.getConfigValue(ConfigParam.NAMENODE_WEB_ADDRESS, ""));
        context.put(RequestContext.NAMENODE_REQUEST_FILTER.name(), "?qry=Hadoop:service=NameNode,name=NameNodeInfo");
        return context;
    }
}
