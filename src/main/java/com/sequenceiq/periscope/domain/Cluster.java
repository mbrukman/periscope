package com.sequenceiq.periscope.domain;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.periscope.log.Logger;
import com.sequenceiq.periscope.log.PeriscopeLoggerFactory;
import com.sequenceiq.periscope.model.AmbariStack;
import com.sequenceiq.periscope.model.HostResolution;
import com.sequenceiq.periscope.model.Priority;
import com.sequenceiq.periscope.model.SchedulerApplication;
import com.sequenceiq.periscope.registry.ClusterState;
import com.sequenceiq.periscope.registry.ConnectionException;
import com.sequenceiq.periscope.service.configuration.AmbariConfigurationService;
import com.sequenceiq.periscope.service.configuration.ConfigParam;

@Entity
public class Cluster {

    private static final Logger LOGGER = PeriscopeLoggerFactory.getLogger(Cluster.class);
    private static final int DEFAULT_MIN_SIZE = 2;
    private static final int DEFAULT_MAX_SIZE = 100;
    private static final int DEFAULT_COOLDOWN = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_generator")
    @SequenceGenerator(name = "template_generator", sequenceName = "sequence_table")
    private long id;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Ambari ambari;
    @ManyToOne
    private PeriscopeUser user;
    @Enumerated(EnumType.STRING)
    private ClusterState state = ClusterState.RUNNING;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "cluster_id")
    private List<MetricAlarm> metricAlarms = new ArrayList<>();
    @JoinColumn(nullable = true, name = "cluster_id")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TimeAlarm> timeAlarms = new ArrayList<>();
    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;
    private int coolDown = DEFAULT_COOLDOWN;
    private Long stackId;

    @Transient
    private Map<String, String> configCache = new HashMap<>();
    @Transient
    private Map<Priority, Map<ApplicationId, SchedulerApplication>> applications;
    @Transient
    private volatile long lastScalingActivity;
    @Transient
    private YarnClient yarnClient;
    @Transient
    private HostResolution resolution;

    public Cluster() {
        this.applications = new ConcurrentHashMap<>();
    }

    public Cluster(PeriscopeUser user, AmbariStack stack, HostResolution resolution) throws ConnectionException {
        this.user = user;
        this.stackId = stack.getStackId();
        this.ambari = stack.getAmbari();
        this.applications = new ConcurrentHashMap<>();
        this.resolution = resolution;
    }

    public void start() throws ConnectionException {
        try {
            if (yarnClient != null) {
                yarnClient.stop();
            }
            yarnClient = YarnClient.createYarnClient();
            yarnClient.init(getConfiguration());
            yarnClient.start();
            configCache = new HashMap<>();
        } catch (Exception e) {
            throw new ConnectionException(getHost());
        }
    }

    public Configuration getConfiguration() throws ConnectException {
        return AmbariConfigurationService.getConfiguration(id, newAmbariClient(), resolution);
    }

    public void stop() {
        if (yarnClient != null) {
            yarnClient.stop();
            yarnClient = null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setResolution(HostResolution resolution) {
        this.resolution = resolution;
    }

    public ClusterState getState() {
        return state;
    }

    public void setState(ClusterState state) {
        this.state = state;
    }

    public List<BaseAlarm> getAlarms() {
        List<BaseAlarm> alarms = new ArrayList<>();
        alarms.addAll(timeAlarms);
        alarms.addAll(metricAlarms);
        return alarms;
    }

    public void setAlarms(List<BaseAlarm> alarms) {
        List<TimeAlarm> timeAlarms = new ArrayList<>();
        List<MetricAlarm> metricAlarms = new ArrayList<>();
        for (BaseAlarm alarm : alarms) {
            if (alarm instanceof TimeAlarm) {
                timeAlarms.add((TimeAlarm) alarm);
                setTimeAlarms(timeAlarms);
            } else {
                metricAlarms.add((MetricAlarm) alarm);
                setMetricAlarms(metricAlarms);
            }
        }
    }

    public List<MetricAlarm> getMetricAlarms() {
        return metricAlarms;
    }

    public void setMetricAlarms(List<MetricAlarm> metricAlarms) {
        this.metricAlarms = metricAlarms;
    }

    public List<TimeAlarm> getTimeAlarms() {
        return timeAlarms;
    }

    public void setTimeAlarms(List<TimeAlarm> timeAlarms) {
        this.timeAlarms = timeAlarms;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
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

    public String getAmbariUser() {
        return ambari.getUser();
    }

    public String getAmbariPass() {
        return ambari.getPass();
    }

    public Ambari getAmbari() {
        return ambari;
    }

    public void setAmbari(Ambari ambari) {
        this.ambari = ambari;
    }

    public Long getStackId() {
        return stackId;
    }

    public void setStackId(Long stackId) {
        this.stackId = stackId;
    }

    public PeriscopeUser getUser() {
        return user;
    }

    public void setUser(PeriscopeUser user) {
        this.user = user;
    }

    public boolean isRunning() {
        return state == ClusterState.RUNNING;
    }

    public String getConfigValue(ConfigParam param, String defaultValue) {
        String key = param.key();
        String value = configCache.get(key);
        if (value == null) {
            try {
                value = getConfiguration().get(key, defaultValue);
                configCache.put(key, value);
            } catch (ConnectException e) {
                value = defaultValue;
            }
        }
        return value;
    }

    public synchronized long getLastScalingActivity() {
        return lastScalingActivity;
    }

    public synchronized void setLastScalingActivityCurrent() {
        this.lastScalingActivity = System.currentTimeMillis();
    }

    public void refreshConfiguration() throws ConnectionException {
        start();
    }

    public synchronized SchedulerApplication addApplication(ApplicationReport appReport) {
        return addApplication(appReport, Priority.NORMAL);
    }

    public void addAlarm(BaseAlarm alarm) {
        if (alarm instanceof MetricAlarm) {
            metricAlarms.add((MetricAlarm) alarm);
        } else {
            timeAlarms.add((TimeAlarm) alarm);
        }
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
        LOGGER.info(this.id, "Application '{}' added", applicationId.toString());
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
                    LOGGER.info(this.id, "Application '{}' removed", applicationId);
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
        return new AmbariClient(getHost(), getPort(), getAmbariUser(), getAmbariPass());
    }

}
