package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.eventprocessor.processors.main.EventProcessor_AbortCommand;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.ActualIngestCompleted;
import com.geocat.ingester.events.ingest.ActualIngestStartCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.events.ingest.IngestEndpointGroupCommand;
import com.geocat.ingester.model.harvester.MetadataRecordXml;
import com.geocat.ingester.service.IngesterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class EventProcessor_IngestEndpointCommand extends BaseEventProcessor<IngestEndpointCommand> {
    Logger logger = LoggerFactory.getLogger(EventProcessor_IngestEndpointCommand.class);


    @Autowired
    IngesterService ingesterService;

    @Autowired
    MetadataRecordRepo metadataRecordRepo;

    long nRecords;

    int PAGESIZE= 200;

    @Override
    public EventProcessor_IngestEndpointCommand internalProcessing() throws Exception {
        IngestEndpointCommand cmd = getInitiatingEvent();
        nRecords = metadataRecordRepo.countMetadataRecordByEndpointJobId(cmd.getEndpointJobId());
        return this;
    }

    @Override
    public EventProcessor_IngestEndpointCommand externalProcessing() {
        IngestEndpointCommand cmd = getInitiatingEvent();

        return this;
    }

    @Override
    public List<Event> newEventProcessing() throws Exception {
        List<Event> result =  new ArrayList<>();
        IngestEndpointCommand cmd = getInitiatingEvent();

        //for example, for getting 10 records;
        //  first one - 1 to 10 (start at 1, get 10)
        //  2nd       - 11 to 20 (start at 11, get 10)
        int pageNumber =0;
        for (int idx = 1; idx <= nRecords; idx += PAGESIZE) {
            long start = idx;
            long end = idx + PAGESIZE - 1;
            if (end > nRecords)
                end = nRecords;
            boolean lastOne = (end == nRecords);
            Event e = new IngestEndpointGroupCommand(cmd.getJobId(),
                    cmd.getHarvesterJobId(),
                    cmd.getEndpointJobId(),
                    pageNumber++,
                    PAGESIZE,
                    start,
                    end
                    );
           result.add(e);
        }

        return result;
    }

}
