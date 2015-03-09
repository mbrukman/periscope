package com.sequenceiq.periscope.monitor.request;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.sequenceiq.periscope.log.Logger;
import com.sequenceiq.periscope.log.PeriscopeLoggerFactory;
import com.sequenceiq.periscope.monitor.event.UpdateFailedEvent;

@Component("DFSMetricRequest")
@Scope("prototype")
public class DFSMetricRequest extends AbstractEventPublisher implements Request {

    private static final Logger LOGGER = PeriscopeLoggerFactory.getLogger(DFSMetricRequest.class);
    private static final String JMX_ENDPOINT = "/jmx";

    @Autowired
    private RestOperations restOperations;
    private long clusterId;
    private String nnWebAddress;
    private String filter;

    @Override
    public void setContext(Map<String, Object> context) {
        this.clusterId = (long) context.get(RequestContext.CLUSTER_ID.name());
        this.nnWebAddress = (String) context.get(RequestContext.NAMENODE_WEB_ADDRESS.name());
        this.filter = (String) context.get(RequestContext.NAMENODE_REQUEST_FILTER.name());
    }

    @Override
    public void run() {
        try {
            String url = "http://" + nnWebAddress + JMX_ENDPOINT + filter;
            Map properties = restOperations.getForObject(url, Map.class);
        } catch (Exception e) {
            LOGGER.error(clusterId, "Error updating the cluster metrics via WebService", e);
            publishEvent(new UpdateFailedEvent(clusterId));
        }
    }
}
