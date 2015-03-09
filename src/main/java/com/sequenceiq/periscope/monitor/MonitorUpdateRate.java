package com.sequenceiq.periscope.monitor;

public final class MonitorUpdateRate {

    /**
     * Every 5 minutes.
     */
    public static final String APP_REPORT_UPDATE_RATE_CRON = "0 0/5 * * * ?";

    /**
     * Every minutes.
     */
    public static final String DFS_UPDATE_RATE_CRON = "0 0/1 * * * ?";

    /**
     * Every 20 seconds.
     */
    public static final String YARN_UPDATE_RATE_CRON = "0/20 * * * * ?";

    /**
     * Every 10 seconds.
     */
    public static final String TIME_UPDATE_RATE_CRON = "0/10 * * * * ?";

    /**
     * Time update rate in ms, aligned to the cron expression.
     */
    public static final int CLUSTER_UPDATE_RATE = 10_000;

    private MonitorUpdateRate() {
        throw new IllegalStateException();
    }
}
