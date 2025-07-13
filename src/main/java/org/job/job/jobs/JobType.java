package org.job.job.jobs;

public enum JobType {
    FARMER("농부"),
    MINER("광부"),
    FISHERMAN("어부");

    private final String jobName;

    JobType(String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }
}
