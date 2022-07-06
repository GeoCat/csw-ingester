package com.geocat.ingester.events.ingest;

import com.geocat.ingester.events.Event;

//job to ingest a few (pageSize) records for an endpoint
public class IngestEndpointGroupCommand extends Event {

    private String harvesterJobId;
    private String jobId;
    private long endpointJobId;
    private long from ;
    private long to  ;
    private long pageNumber;
    private long pageSize;


    public IngestEndpointGroupCommand() {
    }

    public IngestEndpointGroupCommand(String jobId,
                                      String harvesterJobId,
                                      long endpointJobId,
                                      long pageNumber,
                                      long pageSize,
                                      long from,
                                      long to) {
        this.jobId = jobId;
        this.harvesterJobId = harvesterJobId;
        this.endpointJobId = endpointJobId;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.from = from;
        this.to = to;
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

    public long getEndpointJobId() {
        return endpointJobId;
    }

    public void setEndpointJobId(long endpointJobId) {
        this.endpointJobId = endpointJobId;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    //--
    @Override
    public String toString() {
        return "IngestEndpointGroupCommand for processID=" + jobId
                +", harvesterJobId="+harvesterJobId  +", endpointJobId="+endpointJobId
                +", pageSize="+pageSize+", pageNumber="+pageNumber
                +" (from="+from+", to="+to+")";
    }

}
