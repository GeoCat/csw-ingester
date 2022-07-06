package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.ActualIngestStartCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.model.harvester.EndpointJob;
import com.geocat.ingester.service.IngesterService;
import jdk.jfr.EventFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Scope("prototype")
public class EventProcessor_ActualIngestStartCommand extends BaseEventProcessor<ActualIngestStartCommand> {

    @Autowired
    IngesterService ingesterService;


    boolean ingesterServiceComplete = false;

    List<EndpointJob> endpointJobs;

    public EventProcessor_ActualIngestStartCommand() {
        super();
    }

    @Override
    public EventProcessor_ActualIngestStartCommand internalProcessing() throws Exception {
        ActualIngestStartCommand cmd = getInitiatingEvent();

        endpointJobs= ingesterService.findEndpointJobs(cmd.getJobId(),cmd.getHarvesterJobId());
        if (endpointJobs.isEmpty()) {
            throw new Exception("no endpoint jobs found - was there nothing harvested?");
        }
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
            Event e = new IngestEndpointCommand(cmd.getJobId(),cmd.getHarvesterJobId(),endpointJob.getEndpointJobId());
            result.add(e);
        }

        return result;
    }
}
