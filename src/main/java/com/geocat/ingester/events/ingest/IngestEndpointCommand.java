package com.geocat.ingester.events.ingest;

import com.geocat.ingester.events.Event;

//job to ingest an endpoint
// In general, there's only one endpoint in a job.
// However, for Poland there could be multiple
public class IngestEndpointCommand extends Event {

    private String harvesterJobId;
    private String jobId;
    String linkCheckJobId;
    private long endpointJobId;

    public IngestEndpointCommand() {
    }

    public IngestEndpointCommand(String jobId, String harvesterJobId,String linkCheckJobId,long endpointJobId) {
        this.jobId = jobId;
        this.harvesterJobId = harvesterJobId;
        this.endpointJobId = endpointJobId;
        this.linkCheckJobId = linkCheckJobId;
    }

    //--


    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public String getHarvesterJobId() {
        return harvesterJobId;
    }

    public void setHarvesterJobId(String harvesterJobId) {
        this.harvesterJobId = harvesterJobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public long getEndpointJobId() {
        return endpointJobId;
    }

    public void setEndpointJobId(long endpointJobId) {
        this.endpointJobId = endpointJobId;
    }

    //--
    @Override
    public String toString() {
        return "IngestEndpointCommand for processID=" + jobId
                +", harvesterJobId="+harvesterJobId  +", endpointJobId="+endpointJobId;
    }

}
