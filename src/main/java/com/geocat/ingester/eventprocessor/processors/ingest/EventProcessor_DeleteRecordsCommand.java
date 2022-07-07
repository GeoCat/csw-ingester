package com.geocat.ingester.eventprocessor.processors.ingest;

import com.geocat.ingester.dao.harvester.HarvestJobRepo;
import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.eventprocessor.BaseEventProcessor;
import com.geocat.ingester.events.Event;
import com.geocat.ingester.events.ingest.ActualIngestCompleted;
import com.geocat.ingester.events.ingest.DeleteRecordsCommand;
import com.geocat.ingester.events.ingest.IngestEndpointCommand;
import com.geocat.ingester.model.harvester.HarvestJob;
import com.geocat.ingester.model.ingester.IngestJobState;
import com.geocat.ingester.model.metadata.HarvesterConfiguration;
import com.geocat.ingester.service.CatalogueService;
import com.geocat.ingester.service.IngestJobService;
import com.geocat.ingester.service.IngesterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class EventProcessor_DeleteRecordsCommand extends BaseEventProcessor<DeleteRecordsCommand> {

    Logger logger = LoggerFactory.getLogger(EventProcessor_IngestEndpointCommand.class);


    @Autowired
    IngesterService ingesterService;

    @Autowired
    MetadataRecordRepo metadataRecordRepo;

    @Autowired
    CatalogueService catalogueService;

    @Autowired
    HarvestJobRepo harvestJobRepo;

    @Autowired
    IngestJobService ingestJobService;


    List<String> toRemove;



    @Override
    public EventProcessor_DeleteRecordsCommand externalProcessing() throws Exception {
        DeleteRecordsCommand cmd = getInitiatingEvent();

        Optional<HarvestJob> harvestJob = harvestJobRepo.findById(cmd.getHarvesterJobId());
        if (!harvestJob.isPresent()) {
            throw new Exception("No harvester job related found with harvest job id " +  cmd.getHarvesterJobId() + ".");
        }

        Optional<HarvesterConfiguration> harvesterConfigurationOptional =
                catalogueService.retrieveHarvesterConfiguration(harvestJob.get().getLongTermTag());



        List<String> allHarvestedRecordIds =  metadataRecordRepo.findAllRecordIdentifier(cmd.getHarvesterJobId());



        List<String> localHarvesterUuids =
                catalogueService.retrieveLocalUuidsForHarvester(harvesterConfigurationOptional.get().getUuid());
        HashSet<String> localHarvesterUuids_set = new HashSet<>(localHarvesterUuids);
        localHarvesterUuids.clear();//gc

        localHarvesterUuids_set.removeAll(allHarvestedRecordIds);

        toRemove = new ArrayList(localHarvesterUuids_set);
        localHarvesterUuids_set.clear();//gc

        return this;
    }

    @Override
    public EventProcessor_DeleteRecordsCommand internalProcessing() throws Exception {
        DeleteRecordsCommand cmd = getInitiatingEvent();
        ingesterService.deleteRecords(toRemove,cmd.getJobId());
        ingestJobService.updateIngestJobStateInDBDeletedRecords(cmd.getJobId(), toRemove.size());
        return this;
    }


    @Override
    public List<Event> newEventProcessing() throws Exception {
        DeleteRecordsCommand cmd = getInitiatingEvent();

        ingestJobService.updateIngestJobStateInDB(cmd.getJobId(), IngestJobState.RECORDS_PROCESSED);

        List<Event> result =  new ArrayList<>();
        result.add(new ActualIngestCompleted(cmd.getJobId()));
        return result;
    }
}
