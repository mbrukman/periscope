package com.sequenceiq.periscope.monitor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.monitor.request.ApplicationMetricRequest;
import com.sequenceiq.periscope.monitor.request.RequestContext;
import com.sequenceiq.periscope.service.configuration.ConfigParam;

@Component
public class ApplicationMonitor extends AbstractMonitor implements Monitor {

    @Override
    public String getIdentifier() {
        return "application-monitor";
    }

    @Override
    public String getTriggerExpression() {
        return MonitorUpdateRate.APP_REPORT_UPDATE_RATE_CRON;
    }

    @Override
    public Class getRequestType() {
        return ApplicationMetricRequest.class;
    }

    @Override
    public Map<String, Object> getRequestContext(Cluster cluster) {
        Map<String, Object> context = new HashMap<>();
        context.put(RequestContext.CLUSTER_ID.name(), cluster.getId());
        context.put(RequestContext.YARN_CLIENT.name(), cluster.getYarnClient());
        context.put(RequestContext.YARN_RM_WEB_ADDRESS.name(), cluster.getConfigValue(ConfigParam.YARN_RM_WEB_ADDRESS, ""));
        return context;
    }

}
