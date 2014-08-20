package com.sequenceiq.periscope.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.sequenceiq.periscope.jpa.StringIdGenerator;

@Entity
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stringGenerator")
    @GenericGenerator(name = "stringGenerator", strategy = "com.sequenceiq.periscope.jpa.StringIdGenerator",
            parameters = @Parameter(name = StringIdGenerator.FIELD_NAME, value = "alarmName"))
    private String id;
    private String alarmName;
    private String description;
    private Metric metric;
    private double threshold;
    private ComparisonOperator comparisonOperator;
    private int period;
    @Transient
    private long alarmHitsSince;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ScalingPolicy scalingPolicy;

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public long getAlarmHitsSince() {
        return alarmHitsSince;
    }

    public void setAlarmHitsSince(long alarmHitsSince) {
        this.alarmHitsSince = alarmHitsSince;
    }

    public void resetAlarmHitsSince() {
        setAlarmHitsSince(0);
    }

    public ScalingPolicy getScalingPolicy() {
        return scalingPolicy;
    }

    public void setScalingPolicy(ScalingPolicy scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, "alarmHitsSince", "scalingPolicy");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "alarmHitsSince", "scalingPolicy");
    }
}