package com.geocat.ingester.model.ingester;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// create table ingest_job (long_term_tag text, job_id varchar(40), state varchar(40), messages text);
@Entity
public class IngestJob {
    @Id
    @Column(columnDefinition = "varchar(40)")
    private String jobId;
    @Column(columnDefinition = "text")
    private String messages;
    private String longTermTag;
    @Enumerated(EnumType.STRING)
    private IngestJobState state;
    private Long totalRecords;
    private Long totalIngestedRecords;
    private Long totalIndexedRecords;
    @Column(columnDefinition = "timestamp with time zone")
    ZonedDateTime createTimeUTC;
    @Column(columnDefinition = "timestamp with time zone")
    ZonedDateTime lastUpdateUTC;

    @PrePersist
    private void onInsert() {
        this.createTimeUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        this.lastUpdateUTC = this.createTimeUTC;
    }

    @PreUpdate
    private void onUpdate() {
        this.lastUpdateUTC = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public ZonedDateTime getCreateTimeUTC() {
        return createTimeUTC;
    }

    public ZonedDateTime getLastUpdateUTC() {
        return lastUpdateUTC;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getLongTermTag() {
        return longTermTag;
    }

    public void setLongTermTag(String longTermTag) {
        this.longTermTag = longTermTag;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public IngestJobState getState() {
        return state;
    }

    public void setState(IngestJobState state) {
        this.state = state;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Long getTotalIngestedRecords() {
        return totalIngestedRecords;
    }

    public void setTotalIngestedRecords(Long totalIngestedRecords) {
        this.totalIngestedRecords = totalIngestedRecords;
    }

    public Long getTotalIndexedRecords() {
        return totalIndexedRecords;
    }

    public void setTotalIndexedRecords(Long totalIndexedRecords) {
        this.totalIndexedRecords = totalIndexedRecords;
    }
}