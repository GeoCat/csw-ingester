package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.IndexRecordsCommand;
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
        List<Event> result =  new ArrayList<>();

        return result;
    }

}
