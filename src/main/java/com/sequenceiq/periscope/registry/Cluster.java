package com.sequenceiq.periscope.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.periscope.model.Alarm;
import com.sequenceiq.periscope.model.Ambari;
import com.sequenceiq.periscope.model.AutoScalingGroup;
import com.sequenceiq.periscope.model.Priority;
import com.sequenceiq.periscope.model.SchedulerApplication;
import com.sequenceiq.periscope.service.configuration.AmbariConfigurationService;
import com.sequenceiq.periscope.service.configuration.ConfigParam;

public class Cluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);
    private final Map<Priority, Map<ApplicationId, SchedulerApplication>> applications;
    private final String id;
    private final Ambari ambari;
    private boolean appMovementAllowed = true;
    private boolean restarting;
    private Configuration configuration;
    private YarnClient yarnClient;
    private ClusterMetricsInfo metrics;
    private ClusterState state = ClusterState.RUNNING;
    private List<Alarm> alarms = new ArrayList<>();
    private AutoScalingGroup autoScalingGroup;

    public Cluster(String id, Ambari ambari) throws ConnectionException {
        this.id = id;
        this.ambari = ambari;
        this.applications = new ConcurrentHashMap<>();
        initConfiguration();
    }

    public String getId() {
        return id;
    }

    public YarnClient getYarnClient() {
        return yarnClient;
    }

    public String getHost() {
        return ambari.getHost();
    }

    public String getPort() {
        return ambari.getPort();
    }

    public boolean isAppMovementAllowed() {
        return appMovementAllowed;
    }

    public ClusterState getState() {
        return state;
    }

    public void setState(ClusterState state) {
        this.state = state;
    }

    public boolean isRunning() {
        return state == ClusterState.RUNNING;
    }

    public void allowAppMovement(boolean appMovementAllowed) {
        this.appMovementAllowed = appMovementAllowed;
    }

    public String getConfigValue(ConfigParam param, String defaultValue) {
        return configuration.get(param.key(), defaultValue);
    }

    public int getTotalNodes() {
        return metrics == null ? 0 : metrics.getTotalNodes();
    }

    public long getTotalMB() {
        return metrics == null ? 0 : metrics.getTotalMB();
    }

    public boolean isRestarting() {
        return restarting;
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
    }

    public void updateMetrics(ClusterMetricsInfo metrics) {
        this.restarting = false;
        this.metrics = metrics == null ? this.metrics : metrics;
    }

    public void refreshConfiguration() throws ConnectionException {
        initConfiguration();
    }

    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public void setAutoScalingGroup(AutoScalingGroup autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
    }

    public synchronized SchedulerApplication addApplication(ApplicationReport appReport) {
        return addApplication(appReport, Priority.NORMAL);
    }

    public synchronized SchedulerApplication addApplication(ApplicationReport appReport, Priority priority) {
        SchedulerApplication application = new SchedulerApplication(appReport, priority);
        return addApplication(application, priority);
    }

    public synchronized SchedulerApplication addApplication(SchedulerApplication application, Priority priority) {
        Map<ApplicationId, SchedulerApplication> applicationMap = applications.get(priority);
        if (applicationMap == null) {
            applicationMap = new TreeMap<>();
            applications.put(priority, applicationMap);
        }
        ApplicationId applicationId = application.getApplicationId();
        applicationMap.put(applicationId, application);
        LOGGER.info("Application ({}) added to cluster {}", applicationId.toString(), this.id);
        return application;
    }

    public synchronized SchedulerApplication removeApplication(ApplicationId applicationId) {
        for (Priority priority : applications.keySet()) {
            Map<ApplicationId, SchedulerApplication> apps = applications.get(priority);
            Iterator<ApplicationId> iterator = apps.keySet().iterator();
            while (iterator.hasNext()) {
                ApplicationId id = iterator.next();
                if (id.equals(applicationId)) {
                    SchedulerApplication application = apps.get(id);
                    iterator.remove();
                    LOGGER.info("Application ({}) removed from cluster {}", applicationId, this.id);
                    return application;
                }
            }
        }
        return null;
    }

    public synchronized SchedulerApplication setApplicationPriority(ApplicationId applicationId, Priority newPriority) {
        SchedulerApplication application = removeApplication(applicationId);
        if (application != null) {
            application.setPriority(newPriority);
            addApplication(application, newPriority);
        }
        return application;
    }

    public Map<ApplicationId, SchedulerApplication> getApplications(Priority priority) {
        Map<ApplicationId, SchedulerApplication> copy = new TreeMap<>();
        Map<ApplicationId, SchedulerApplication> apps = applications.get(priority);
        if (apps != null) {
            copy.putAll(apps);
        }
        return copy;
    }

    public Map<Priority, Map<ApplicationId, SchedulerApplication>> getApplicationsPriorityOrder() {
        Map<Priority, Map<ApplicationId, SchedulerApplication>> copy = new TreeMap<>();
        for (Priority priority : applications.keySet()) {
            copy.put(priority, new TreeMap<>(applications.get(priority)));
        }
        return copy;
    }

    public SchedulerApplication getApplication(ApplicationId applicationId) {
        for (Priority priority : applications.keySet()) {
            Map<ApplicationId, SchedulerApplication> apps = applications.get(priority);
            for (ApplicationId id : apps.keySet()) {
                if (applicationId.equals(id)) {
                    return apps.get(id);
                }
            }
        }
        return null;
    }

    public AmbariClient newAmbariClient() {
        return new AmbariClient(ambari.getHost(), ambari.getPort(), ambari.getUser(), ambari.getPass());
    }

    private void initConfiguration() throws ConnectionException {
        try {
            configuration = AmbariConfigurationService.getConfiguration(newAmbariClient());
            if (yarnClient != null) {
                yarnClient.stop();
            }
            yarnClient = YarnClient.createYarnClient();
            yarnClient.init(configuration);
            yarnClient.start();
        } catch (Exception e) {
            throw new ConnectionException(ambari.getHost());
        }
    }

}
