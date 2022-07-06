package com.geocat.ingester.events.ingest;

import com.geocat.ingester.events.Event;

public class IndexRecordsCommand extends Event {


    private String harvesterJobId;
    private String jobId;

    public IndexRecordsCommand() {
    }

    public IndexRecordsCommand(String jobId, String harvesterJobId,String endpointJobId) {
        this.jobId = jobId;
        this.harvesterJobId = harvesterJobId;
    }

    //--


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

    //--
    @Override
    public String toString() {
        return "IndexRecordsCommand for processID=" + jobId
                +", harvesterJobId="+harvesterJobId  ;
    }


}
