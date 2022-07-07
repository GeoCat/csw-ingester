package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.dao.ingester.IngestEndpointGroupRepo;
import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.IndexRecordsCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.events.ingest.IngestEndpointGroupCommand;
import com.geocat.ingester.model.ingester.IngestEndpointGroup;
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
public class EventProcessor_IngestEndpointGroupCommand extends BaseEventProcessor<IngestEndpointGroupCommand> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_IngestEndpointCommand.class);

    @Autowired
    IngestEndpointGroupRepo ingestEndpointGroupRepo;

    @Autowired
    IngesterService ingesterService;

    @Autowired
    IngestJobService ingestJobService;

    IngestEndpointGroup ingestEndpointGroup;


    void clean() {
        IngestEndpointGroupCommand cmd = getInitiatingEvent();
        //todo - if this message is being processed multiple times, then clean out any of the previous work
        if (ingestEndpointGroup.getNumberRecordsIngested() != null) {
            //this should be null
            ingestEndpointGroup.setNumberRecordsIngested(null);
            ingestEndpointGroup = ingestEndpointGroupRepo.save(ingestEndpointGroup);
        }
    }



    @Override
    public EventProcessor_IngestEndpointGroupCommand externalProcessing() {
        IngestEndpointGroupCommand cmd = getInitiatingEvent();
        ingestEndpointGroup = ingestEndpointGroupRepo.findById(cmd.getIngestEndpointGroupId()).get();
        return this;
    }

    @Override
    public EventProcessor_IngestEndpointGroupCommand internalProcessing() throws Exception {
        IngestEndpointGroupCommand cmd = getInitiatingEvent();
        clean();
        ingestEndpointGroup = ingesterService.ingestGroup(ingestEndpointGroup);
        return this;
    }

    @Override
    public List<Event> newEventProcessing() throws Exception {
        List<Event> result =  new ArrayList<>();
        IngestEndpointGroupCommand cmd = getInitiatingEvent();

        if (ingesterService.ingestEndpointGroupsComplete(cmd.getJobId(),cmd.getEndpointJobId())
             && ingesterService.ingestComplete(cmd.getJobId())) {
            Event e = new IndexRecordsCommand(cmd.getJobId(),cmd.getHarvesterJobId());
            result.add(e);
            ingestJobService.updateIngestJobStateInDB(cmd.getJobId(), IngestJobState.INDEXING_RECORDS);
        }
        return result;
    }

}
