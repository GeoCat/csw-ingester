package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.dao.ingester.IngestEndpointGroupRepo;
import com.geocat.ingester.dao.ingester.IngestJobRepo;
import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.eventprocessor.processors.main.EventProcessor_AbortCommand;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.ActualIngestCompleted;
import com.geocat.ingester.events.ingest.ActualIngestStartCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.events.ingest.IngestEndpointGroupCommand;
import com.geocat.ingester.model.harvester.MetadataRecordXml;
import com.geocat.ingester.model.ingester.IngestEndpointGroup;
import com.geocat.ingester.model.ingester.IngestJobState;
import com.geocat.ingester.service.IngestJobService;
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

    @Autowired
    IngestEndpointGroupRepo ingestEndpointGroupRepo;

    @Autowired
    IngestJobService ingestJobService;

    long nRecords;
    List<IngestEndpointGroupCommand> events;
    List<IngestEndpointGroup> objects;

    int PAGESIZE= 200;

    public void clean() {
        IngestEndpointCommand cmd = getInitiatingEvent();
        long n = ingestEndpointGroupRepo.deleteByEndpointId(cmd.getEndpointJobId());
    }

    @Override
    public EventProcessor_IngestEndpointCommand internalProcessing() throws Exception {
        IngestEndpointCommand cmd = getInitiatingEvent();
        clean();
        List<IngestEndpointGroup> toSaveObjects = new ArrayList<>();
        for (IngestEndpointGroupCommand event : events) {
            IngestEndpointGroup object = new IngestEndpointGroup(event);
            toSaveObjects.add(object);
        }
        // bulk save (and put saved objects in list)
        Iterable<IngestEndpointGroup> saved = ingestEndpointGroupRepo.saveAll(toSaveObjects);
        objects = new ArrayList<>();
        saved.forEach(objects::add);

        // put the saved object's ID inside the event
        for (int t=0;t<events.size(); t++) {
            IngestEndpointGroupCommand event = events.get(t);
            IngestEndpointGroup object = objects.get(t);
            event.setIngestEndpointGroupId(object.getIngestEndpointGroupId());
        }

        return this;
    }

    @Override
    public EventProcessor_IngestEndpointCommand externalProcessing () {
        IngestEndpointCommand cmd = getInitiatingEvent();
        nRecords = metadataRecordRepo.countMetadataRecordByEndpointJobId(cmd.getEndpointJobId());
        //for example, for getting 10 records;
        //  first one - 1 to 10 (start at 1, get 10)
        //  2nd       - 11 to 20 (start at 11, get 10)
        int pageNumber =0;
        events = new ArrayList<>();
        for (int idx = 1; idx <= nRecords; idx += PAGESIZE) {
            long start = idx;
            long end = idx + PAGESIZE - 1;
            if (end > nRecords)
                end = nRecords;
            boolean lastOne = (end == nRecords);
            IngestEndpointGroupCommand e = new IngestEndpointGroupCommand(cmd.getJobId(),
                    cmd.getHarvesterJobId(),
                    cmd.getLinkCheckJobId(),
                    cmd.getEndpointJobId(),
                    pageNumber++,
                    PAGESIZE,
                    start,
                    end
            );
            events.add(e);
        }
        return this;
    }

    @Override
    public List<Event> newEventProcessing() throws Exception {
        ingestJobService.updateIngestJobStateInDB(getInitiatingEvent().getJobId(), IngestJobState.INGESTING_RECORDS);

        return (List<Event>) (List<? extends Event>) events; //double cast to change list type
    }

}
