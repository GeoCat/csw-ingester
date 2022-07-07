package com.geocat.ingester.dao.ingester;

import com.geocat.ingester.model.ingester.IngestEndpointGroup;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public interface IngestEndpointGroupRepo extends CrudRepository<IngestEndpointGroup, Long> {

    @Query("SELECT COUNT(ug) FROM IngestEndpointGroup ug WHERE  ug.ingestJobId=?1 and ug.endpointId=?2 and ug.numberRecordsIngested is null")
    long countUncompleteGroupsByEndpoint(String ingestJobId,long endpointId);

    @Query("SELECT COUNT(ug) FROM IngestEndpointGroup ug WHERE ug.ingestJobId=?1 and ug.numberRecordsIngested is null")
    long countUncompleteGroupsForJob(String ingestJobId);

    @Query("SELECT sum(ug.numberRecordsIngested) FROM IngestEndpointGroup ug WHERE ug.ingestJobId=?1 and ug.numberRecordsIngested is not null")
    long countCompleteIngestRecordsForJob(String ingestJobId);

    long deleteByEndpointId(long endpointId);
    long deleteByIngestJobId(String injectJobId);

}
