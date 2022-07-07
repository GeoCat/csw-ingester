package com.geocat.ingester.model.ingester;

import com.geocat.ingester.events.ingest.IngestEndpointGroupCommand;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "ingestendpointgroup",
        indexes = {
                @Index(
                        name = "ingestendpointgroup_endpointId_idx",
                        columnList = "endpointId",
                        unique = false
                )
        })
public class IngestEndpointGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ingestEndpointGroupId;

    private String ingestJobId;

    private String linkCheckJobId;
    private String harvestJobId;
    private long endpointId;

    private long fromRecordNumber;
    private long toRecordNumber;
    private long pageNumber;
    private long pageSize;
    private Long numberRecordsIngested; // null = none


    public IngestEndpointGroup(){
    }

    public IngestEndpointGroup(IngestEndpointGroupCommand event){
        this.ingestJobId = event.getJobId();
        this.endpointId = event.getEndpointJobId();
        this.fromRecordNumber = event.getFrom();
        this.toRecordNumber = event.getTo();
        this.pageNumber = event.getPageNumber();
        this.pageSize = event.getPageSize();
        this.linkCheckJobId = event.getLinkCheckJobId();
        this.harvestJobId = event.getHarvesterJobId();
        this.numberRecordsIngested = null;
    }

    //---


    public String getHarvestJobId() {
        return harvestJobId;
    }

    public void setHarvestJobId(String harvestJobId) {
        this.harvestJobId = harvestJobId;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }

    public long getIngestEndpointGroupId() {
        return ingestEndpointGroupId;
    }

    public void setIngestEndpointGroupId(long ingestEndpointGroupId) {
        this.ingestEndpointGroupId = ingestEndpointGroupId;
    }

    public String getIngestJobId() {
        return ingestJobId;
    }

    public void setIngestJobId(String ingestJobId) {
        this.ingestJobId = ingestJobId;
    }

    public long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(long endpointId) {
        this.endpointId = endpointId;
    }

    public long getFromRecordNumber() {
        return fromRecordNumber;
    }

    public void setFromRecordNumber(long from) {
        this.fromRecordNumber = from;
    }

    public long getToRecordNumber() {
        return toRecordNumber;
    }

    public void setToRecordNumber(long to) {
        this.toRecordNumber = to;
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

    public Long getNumberRecordsIngested() {
        return numberRecordsIngested;
    }

    public void setNumberRecordsIngested(Long numberRecordsIngested) {
        this.numberRecordsIngested = numberRecordsIngested;
    }


    //---



}
