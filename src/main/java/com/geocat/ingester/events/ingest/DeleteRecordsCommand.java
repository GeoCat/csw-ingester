package com.geocat.ingester.events.ingest;

import com.geocat.ingester.events.Event;

public class DeleteRecordsCommand extends Event {


    private String harvesterJobId;
    private String jobId;

    public DeleteRecordsCommand() {
    }

    public DeleteRecordsCommand(String jobId, String harvesterJobId ) {
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
        return "DeleteRecordsCommand for processID=" + jobId
                +", harvesterJobId="+harvesterJobId  ;
    }


}
