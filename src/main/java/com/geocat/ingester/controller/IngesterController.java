package com.geocat.ingester.controller;

import com.geocat.ingester.dao.harvester.MetadataRecordRepo;
import com.geocat.ingester.dao.metadata.MetadataRepository;
import com.geocat.ingester.exception.IndexingRecordException;
import com.geocat.ingester.model.metadata.Metadata;
import com.geocat.ingester.service.IngesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
public class IngesterController {

    @Autowired
    private IngesterService ingesterService;

    @Autowired
    private MetadataRepository metadataRepo;

    @RequestMapping(value = "/ingester/{harvesterUuid}", method = RequestMethod.GET)
    public String retrieve(@PathVariable String jobId) {
        return jobId;
    }

    @RequestMapping(value = "/ingester/{harvesterUuid}", method = RequestMethod.PUT)
    public ResponseEntity execute(@PathVariable String harvesterUuid) throws Exception {
        ingesterService.run(harvesterUuid);

        return ResponseEntity.ok().build();
    }
}
