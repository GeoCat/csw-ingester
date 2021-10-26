package com.geocat.ingester.service;

import com.geocat.ingester.dao.harvester.EndpointJobRepo;
import com.geocat.ingester.dao.harvester.HarvestJobRepo;
import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.dao.linkchecker.LinkCheckJobRepo;
import com.geocat.ingester.dao.linkchecker.LocalDatasetMetadataRecordRepo;
import com.geocat.ingester.dao.linkchecker.LocalServiceMetadataRecordRepo;
import com.geocat.ingester.exception.GeoNetworkClientException;
import com.geocat.ingester.geonetwork.client.GeoNetworkClient;
import com.geocat.ingester.model.harvester.EndpointJob;
import com.geocat.ingester.model.harvester.HarvestJob;
import com.geocat.ingester.model.harvester.MetadataRecordXml;
import com.geocat.ingester.model.ingester.IngestJobState;

import com.geocat.ingester.model.linkchecker.LinkCheckJob;
import com.geocat.ingester.model.linkchecker.LocalDatasetMetadataRecord;
import com.geocat.ingester.model.linkchecker.LocalServiceMetadataRecord;
import com.geocat.ingester.model.linkchecker.helper.CapabilitiesType;
import com.geocat.ingester.model.linkchecker.helper.IndicatorStatus;
import com.geocat.ingester.model.metadata.HarvesterConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
//@Transactional("metadataTransactionManager")
//@Slf4j(topic = "com.geocat.ingester.service")
public class IngesterService {

    Logger log = LoggerFactory.getLogger(IngesterService.class);

    @Autowired
    private HarvestJobRepo harvestJobRepo;

    @Autowired
    private EndpointJobRepo endpointJobRepo;

    @Autowired
    private MetadataRecordRepo metadataRecordRepo;

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private GeoNetworkClient geoNetworkClient;

    @Autowired
    private IngestJobService ingestJobService;

    @Autowired
    private LinkCheckJobRepo linkCheckJobRepo;

    @Autowired
    private LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    private LocalDatasetMetadataRecordRepo localDatasetMetadataRecordRepo;

    /**
     * Executes the ingester process.
     *
     * @param harvestJobId
     * @throws Exception
     */
    public void run(String processId, String harvestJobId) throws Exception {
        Optional<HarvestJob> harvestJob = harvestJobRepo.findById(harvestJobId);
        if (!harvestJob.isPresent()) {
            log.info("No harvester job related found with harvest job id " +  harvestJobId + ".");
            // TODO: throw Exception harvester job not found
            return;
        }

        String harvesterUuidOrName = harvestJob.get().getLongTermTag();

        Optional<HarvesterConfiguration> harvesterConfigurationOptional = catalogueService.retrieveHarvesterConfiguration(harvesterUuidOrName);

        if (!harvesterConfigurationOptional.isPresent()) {
            log.info("Harvester with name/uuid " +  harvesterUuidOrName + " not found.");
            // TODO: throw Exception harvester not found
            return;
        }

        log.info("Start ingestion process for harvester with name/uuid " + harvestJob.get().getLongTermTag() + ".");

        Optional<LinkCheckJob> linkCheckJob = linkCheckJobRepo.findByHarvestJobId(harvestJobId);
        String linkCheckJobId = null;
        if (!linkCheckJob.isPresent()) {
            log.info("No link checker job related found for the harvester with name/uuid " +  harvesterUuidOrName + ".");
        } else {
            linkCheckJobId = linkCheckJob.get().getJobId();
        }

        List<EndpointJob> endpointJobList = endpointJobRepo.findByHarvestJobId(harvestJobId);

        long totalMetadataToProcess = 0;
        for (EndpointJob job : endpointJobList) {
            totalMetadataToProcess = totalMetadataToProcess + metadataRecordRepo.countMetadataRecordByEndpointJobId(job.getEndpointJobId());
        }

        ingestJobService.updateIngestJobStateInDBIngestedRecords(processId, 0, 0, 0, totalMetadataToProcess);

        Map<String, Boolean> metadataIds = new HashMap<>();

        try {
            for (EndpointJob job : endpointJobList) {
                Boolean pagesAvailable = true;

                int page = 0;
                int size = 200;

                long total = 0;

                while (pagesAvailable) {
                    Pageable pageableRequest = PageRequest.of(page++, size);

                    Page<MetadataRecordXml> metadataRecordList =
                            metadataRecordRepo.findMetadataRecordWithXmlByEndpointJobId(job.getEndpointJobId(), pageableRequest);

                    if (page == 1) {
                        log.info("Total harvested records to process: " + metadataRecordList.getTotalElements());
                    }

                    long from = (size * (page - 1) ) + 1;
                    long to =  Math.min((size * page) - 1, metadataRecordList.getTotalElements());
                    log.info("Adding harvested metadata records to the catalogue from " +  from + " to " + to + " of " + metadataRecordList.getTotalElements());

                    total = total + metadataRecordList.getNumberOfElements();

                    // Process metadata indicators
                    if (!StringUtils.isEmpty(linkCheckJobId)) {
                        final String linkCheckJobIdAux = linkCheckJobId;

                        metadataRecordList.forEach(r -> {
                            fillMetadataIndicators(r, linkCheckJobIdAux);
                        });
                    }

                    metadataIds.putAll(catalogueService.addOrUpdateMetadataRecords(metadataRecordList.toList(), harvesterConfigurationOptional.get(), harvestJobId));

                    ingestJobService.updateIngestJobStateInDBIngestedRecords(processId, total);

                    pagesAvailable = !metadataRecordList.isLast();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Index added/updated records
        ingestJobService.updateIngestJobStateInDB(processId, IngestJobState.INDEXING_RECORDS);
        List<String> metadataIdsToIndex = metadataIds.entrySet().stream().filter(a -> a.getValue().equals(Boolean.TRUE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        geoNetworkClient.init();
        indexRecords(metadataIdsToIndex, processId);

        // Delete old harvested records no longer in the harvester server
        List<String> remoteHarvesterUuids = metadataIds.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> localHarvesterUuids =
                catalogueService.retrieveLocalUuidsForHarvester(harvesterConfigurationOptional.get().getUuid());

        List<String> metadataIdsToDelete = localHarvesterUuids.stream()
                .distinct()
                .filter(s -> !remoteHarvesterUuids.contains(s))
                .collect(Collectors.toList());

        ingestJobService.updateIngestJobStateInDB(processId, IngestJobState.DELETING_RECORDS);
        deleteRecords(metadataIdsToDelete, processId);

        ingestJobService.updateIngestJobStateInDB(processId, IngestJobState.RECORDS_PROCESSED);

        log.info("Finished ingestion process for harvester with name/uuid " +  harvesterUuidOrName + ".");
    }


    /**
     * Calls GeoNetwork index process for a set of metadata records.
     *
     * @param metadataIds
     * @param processId
     * @throws GeoNetworkClientException
     */
    private void indexRecords(List<String> metadataIds, String processId) {
        int batchSize = 50;

        int totalPages = (int) Math.ceil(metadataIds.size() * 1.0 / batchSize * 1.0);

        int total = 0;

        for (int i = 0; i < totalPages; i++) {
            try {
                int from = i * batchSize;
                int to = Math.min(((i+1) * batchSize), metadataIds.size());

                int toR = (i == totalPages - 1)?metadataIds.size():(to-1);
                log.info("Indexing harvested metadata records from " +  Math.max(1, i * batchSize) + " to " + toR + " of " + metadataIds.size());

                geoNetworkClient.index(metadataIds.subList(from , to));

                total = total + (to-from);

                ingestJobService.updateIngestJobStateInDBIndexedRecords(processId, total);

            } catch (GeoNetworkClientException ex) {
                // TODO: Handle exception
                log.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Calls GeoNetwork delete process for a set of metadata records.
     *
     * @param metadataIds
     * @param processId
     * @throws GeoNetworkClientException
     */
    private void deleteRecords(List<String> metadataIds, String processId) {
        int batchSize = 50;

        int totalPages = (int) Math.ceil(metadataIds.size() * 1.0 / batchSize * 1.0);

        int total = 0;

        for (int i = 0; i < totalPages; i++) {
            try {
                int from = i * batchSize;
                int to = Math.min(((i+1) * batchSize), metadataIds.size());

                int toR = (i == totalPages - 1)?metadataIds.size():(to-1);
                log.info("Indexing harvested metadata records from " +  Math.max(1, i * batchSize) + " to " + toR + " of " + metadataIds.size());

                geoNetworkClient.delete(metadataIds.subList(from , to));

                total = total + (to-from);

                ingestJobService.updateIngestJobStateInDBDeletedRecords(processId, total);

            } catch (Exception ex) {
                // TODO: Handle exception
                log.error(ex.getMessage(), ex);

            }
        }
    }


    /**
     * Fill the indicators associated to a metadata record for a link checker job.
     *
     * @param metadata
     * @param linkCheckJobId
     */
    private void fillMetadataIndicators(MetadataRecordXml metadata, String linkCheckJobId) {
        List<LocalServiceMetadataRecord> localServiceMetadataRecord = localServiceMetadataRecordRepo.findAllByFileIdentifierAndLinkCheckJobId(metadata.getRecordIdentifier(), linkCheckJobId);

        if (!localServiceMetadataRecord.isEmpty()) {
            addIndicator(metadata, "INDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE", localServiceMetadataRecord.get(0).getINDICATOR_ALL_CAPABILITIES_LAYER_RESOLVE());
            addIndicator(metadata, "INDICATOR_ALL_OPERATES_ON_RESOLVE", localServiceMetadataRecord.get(0).getINDICATOR_ALL_OPERATES_ON_RESOLVE());
            addIndicator(metadata, "INDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES", localServiceMetadataRecord.get(0).getINDICATOR_ALL_OPERATES_ON_MATCH_CAPABILITIES());
            addIndicator(metadata, "INDICATOR_CAPABILITIES_TYPE", localServiceMetadataRecord.get(0).getINDICATOR_CAPABILITIES_TYPE());
            addIndicator(metadata, "INDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE", localServiceMetadataRecord.get(0).getINDICATOR_CAPABILITIES_RESOLVES_TO_SERVICE());
            addIndicator(metadata, "INDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES", localServiceMetadataRecord.get(0).getINDICATOR_CAPABILITIES_SERVICE_FILE_ID_MATCHES());
           // addIndicator(metadata, "INDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES", localServiceMetadataRecord.get(0).getINDICATOR_CAPABILITIES_SERVICE_FULLY_MATCHES());
            addIndicator(metadata, "INDICATOR_RESOLVES_TO_CAPABILITIES", localServiceMetadataRecord.get(0).getINDICATOR_RESOLVES_TO_CAPABILITIES());
        } else {
            List<LocalDatasetMetadataRecord> localDatasetMetadataRecord = localDatasetMetadataRecordRepo.findAllByFileIdentifierAndLinkCheckJobId(metadata.getRecordIdentifier(), linkCheckJobId);

            if (!localDatasetMetadataRecord.isEmpty()) {
                addIndicator(metadata, "INDICATOR_CAPABILITIES_TYPE", localDatasetMetadataRecord.get(0).getINDICATOR_CAPABILITIES_TYPE());
                addIndicator(metadata, "INDICATOR_LAYER_MATCHES", localDatasetMetadataRecord.get(0).getINDICATOR_LAYER_MATCHES());
                addIndicator(metadata, "INDICATOR_RESOLVES_TO_CAPABILITIES", localDatasetMetadataRecord.get(0).getINDICATOR_RESOLVES_TO_CAPABILITIES());
                addIndicator(metadata, "INDICATOR_LAYER_MATCHES_VIEW", localDatasetMetadataRecord.get(0).getINDICATOR_LAYER_MATCHES_VIEW());
                addIndicator(metadata, "INDICATOR_LAYER_MATCHES_DOWNLOAD", localDatasetMetadataRecord.get(0).getINDICATOR_LAYER_MATCHES_DOWNLOAD());
            }

        }
    }

    private void addIndicator(MetadataRecordXml metadata, String indicatorName, IndicatorStatus indicatorStatus) {
        if (indicatorStatus != null) {
            metadata.addIndicator(indicatorName, indicatorStatus.toString());
        }
    }

    private void addIndicator(MetadataRecordXml metadata, String indicatorName, CapabilitiesType capabilitiesType) {
        if (capabilitiesType != null) {
            metadata.addIndicator(indicatorName, capabilitiesType.toString());
        }
    }

    private void addIndicator(MetadataRecordXml metadata, String indicatorName, Integer indicator) {
        if (indicator != null) {
            metadata.addIndicator(indicatorName, indicator.toString());
        }
    }
}
