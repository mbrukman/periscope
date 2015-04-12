package com.sequenceiq.periscope.monitor.evaluator;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.periscope.domain.BaseAlert;
import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.domain.MetricAlert;
import com.sequenceiq.periscope.log.Logger;
import com.sequenceiq.periscope.log.PeriscopeLoggerFactory;
import com.sequenceiq.periscope.monitor.event.ScalingEvent;
import com.sequenceiq.periscope.monitor.event.UpdateFailedEvent;
import com.sequenceiq.periscope.repository.MetricAlertRepository;
import com.sequenceiq.periscope.service.ClusterService;
import com.sequenceiq.periscope.utils.ClusterUtils;

@Component("MetricEvaluator")
@Scope("prototype")
public class MetricEvaluator extends AbstractEventPublisher implements EvaluatorExecutor {

    private static final Logger LOGGER = PeriscopeLoggerFactory.getLogger(MetricEvaluator.class);
    private static final String ALERT_STATE = "state";
    private static final String ALERT_TS = "timestamp";

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private MetricAlertRepository alertRepository;

    private long clusterId;

    @Override
    public void setContext(Map<String, Object> context) {
        this.clusterId = (long) context.get(EvaluatorContext.CLUSTER_ID.name());
    }

    @Override
    public void run() {
        Cluster cluster = clusterService.find(clusterId);
        AmbariClient ambariClient = cluster.newAmbariClient();
        try {
            for (MetricAlert alert : alertRepository.findAllByCluster(clusterId)) {
                String alertName = alert.getName();
                LOGGER.info(clusterId, "Checking metric based alert: '{}'", alertName);
                List<Map<String, Object>> alertHistory = ambariClient.getAlertHistory(alert.getDefinitionName(), 1);
                int historySize = alertHistory.size();
                if (historySize > 1) {
                    LOGGER.debug(clusterId, "Multiple results found for alert: {}, probably HOST alert, ignoring now..", alertName);
                    continue;
                }
                Map<String, Object> history = alertHistory.get(0);
                String currentState = (String) history.get(ALERT_STATE);
                if (isAlertStateMet(currentState, alert)) {
                    long elapsedTime = getPeriod(history);
                    LOGGER.info(clusterId, "Alert: {} is in '{}' state since {} min(s)", alertName, currentState,
                            ClusterUtils.TIME_FORMAT.format((double) elapsedTime / ClusterUtils.MIN_IN_MS));
                    if (isPeriodReached(alert, elapsedTime) && isPolicyAttached(alert)) {
                        publishEvent(new ScalingEvent(alert));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(clusterId, "Failed to retrieve alert history", e);
            publishEvent(new UpdateFailedEvent(clusterId));
        }
    }

    private boolean isAlertStateMet(String currentState, MetricAlert alert) {
        return currentState.equalsIgnoreCase(alert.getAlertState().name());
    }

    private long getPeriod(Map<String, Object> history) {
        return System.currentTimeMillis() - (long) history.get(ALERT_TS);
    }

    private boolean isPeriodReached(MetricAlert alert, float period) {
        return period > alert.getPeriod() * ClusterUtils.MIN_IN_MS;
    }

    private boolean isPolicyAttached(BaseAlert alert) {
        return alert.getScalingPolicy() != null;
    }

}