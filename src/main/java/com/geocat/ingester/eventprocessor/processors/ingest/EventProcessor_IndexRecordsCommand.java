package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.DeleteRecordsCommand;
import com.geocat.ingester.events.ingest.IndexRecordsCommand;
import com.geocat.ingester.model.ingester.IngestJobState;
import com.geocat.ingester.service.IngestJobService;
import com.geocat.ingester.service.IngesterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class EventProcessor_IndexRecordsCommand extends BaseEventProcessor<IndexRecordsCommand>  {


    Logger logger = LoggerFactory.getLogger(EventProcessor_IngestEndpointCommand.class);

    @Autowired
    IngestJobService ingestJobService;

    @Autowired
    IngesterService ingesterService;


    @Override
    public EventProcessor_IndexRecordsCommand internalProcessing() throws Exception {
        IndexRecordsCommand cmd = getInitiatingEvent();

        return this;
    }

    @Override
    public EventProcessor_IndexRecordsCommand externalProcessing() {
        IndexRecordsCommand cmd = getInitiatingEvent();

        return this;
    }

    @Override
    public List<Event> newEventProcessing() throws Exception {
        IndexRecordsCommand cmd = getInitiatingEvent();

        List<Event> result =  new ArrayList<>();
        result.add(new DeleteRecordsCommand(cmd.getJobId(), cmd.getHarvesterJobId()));
        ingestJobService.updateIngestJobStateInDB(cmd.getJobId(), IngestJobState.DELETING_RECORDS);

        return result;
    }

}
