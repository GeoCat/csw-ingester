package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.dao.linkchecker.LinkCheckJobRepo;
import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.IngestEventService;
import com.geocat.ingester.events.ingest.ActualIngestStartCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.model.harvester.EndpointJob;
import com.geocat.ingester.model.linkchecker.LinkCheckJob;
import com.geocat.ingester.service.IngestJobService;
import com.geocat.ingester.service.IngesterService;
import jdk.jfr.EventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
@Scope("prototype")
public class EventProcessor_ActualIngestStartCommand extends BaseEventProcessor<ActualIngestStartCommand> {

    Logger log = LoggerFactory.getLogger(EventProcessor_ActualIngestStartCommand.class);


    @Autowired
    IngesterService ingesterService;

    @Autowired
    LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    IngestJobService ingestJobService;

    @Autowired
    MetadataRecordRepo metadataRecordRepo;


    boolean ingesterServiceComplete = false;
    String linkCheckJobId = null;

    List<EndpointJob> endpointJobs;

    public EventProcessor_ActualIngestStartCommand() {
        super();
    }

    @Override
    public EventProcessor_ActualIngestStartCommand internalProcessing() throws Exception {
        ActualIngestStartCommand cmd = getInitiatingEvent();

        Optional<LinkCheckJob> linkCheckJob = linkCheckJobRepo.findByHarvestJobId(cmd.getHarvesterJobId());

        if (!linkCheckJob.isPresent()) {
            log.info("No link checker job related found for the harvester with uuid " +  cmd.getHarvesterJobId() + ".");
        } else {
            linkCheckJobId = linkCheckJob.get().getJobId();
        }

        endpointJobs= ingesterService.findEndpointJobs(cmd.getJobId(),cmd.getHarvesterJobId());
        if (endpointJobs.isEmpty()) {
            throw new Exception("no endpoint jobs found - was there nothing harvested?");
        }

        ingestJobService.updateIngestJobStateInDBIngestedRecords
                (getInitiatingEvent().getJobId(),
                        0,
                        0,
                        0,
                        metadataRecordRepo.countAllRecordIdentifier(getInitiatingEvent().getHarvesterJobId()));

        return this;
    }

    @Override
    public EventProcessor_ActualIngestStartCommand externalProcessing() {
        return this;
    }

    @Override
    public List<Event> newEventProcessing() throws Exception {
        List<Event> result =  new ArrayList<>();
        ActualIngestStartCommand cmd = getInitiatingEvent();

        for (EndpointJob endpointJob : endpointJobs) {
            Event e = new IngestEndpointCommand(cmd.getJobId(),
                    cmd.getHarvesterJobId(),
                    linkCheckJobId,
                    endpointJob.getEndpointJobId());
            result.add(e);
        }

        return result;
    }
}
