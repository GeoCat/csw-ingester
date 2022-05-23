package com.geocat.ingester.eventprocessor.processors.main;

import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.ActualIngestCompleted;
import com.geocat.ingester.model.ingester.IngestJobState;
import com.geocat.ingester.service.IngestJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class EventProcessor_ActualIngestCompleted extends BaseEventProcessor<ActualIngestCompleted> {

    @Autowired
    IngestJobService ingestJobService;

    @Override
    public EventProcessor_ActualIngestCompleted internalProcessing() {
        return this;
    }

    @Override
    public EventProcessor_ActualIngestCompleted externalProcessing() {
        ingestJobService.updateIngestJobStateInDB(getInitiatingEvent().getJobId(), IngestJobState.COMPLETE);
        return this;
    }

    @Override
    public List<Event> newEventProcessing() {
        return new ArrayList<>();
    }

}
