package com.geocat.ingester.events;

public class IngestRequestedEvent extends Event {
    String jobId;
    private String harvestJobId;

    public IngestRequestedEvent() {
    }


    public String getHarvestJobId() {
        return harvestJobId;
    }

    public void setHarvestJobId(String harvestJobId) {
        this.harvestJobId = harvestJobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
