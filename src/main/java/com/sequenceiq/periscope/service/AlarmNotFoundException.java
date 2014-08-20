package com.sequenceiq.periscope.service;

public class AlarmNotFoundException extends RuntimeException {

    private final String id;

    public AlarmNotFoundException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
