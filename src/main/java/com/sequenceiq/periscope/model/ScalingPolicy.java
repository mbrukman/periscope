package com.sequenceiq.periscope.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.sequenceiq.periscope.jpa.StringIdGenerator;

@Entity
public class ScalingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stringGenerator")
    @GenericGenerator(name = "stringGenerator", strategy = "com.sequenceiq.periscope.jpa.StringIdGenerator",
            parameters = @Parameter(name = StringIdGenerator.FIELD_NAME, value = "name"))
    private String id;
    private String name;
    private AdjustmentType adjustmentType;
    private int scalingAdjustment;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Alarm alarm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(AdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public int getScalingAdjustment() {
        return scalingAdjustment;
    }

    public void setScalingAdjustment(int scalingAdjustment) {
        this.scalingAdjustment = scalingAdjustment;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, "alarm");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "alarm");
    }
}
