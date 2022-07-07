package com.geocat.ingester.dao.harvester;


import com.geocat.ingester.model.harvester.MetadataRecord;
import com.geocat.ingester.model.harvester.MetadataRecordXml;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public interface MetadataRecordRepo extends CrudRepository<MetadataRecord, String> {

    List<MetadataRecord> findMetadataRecordByEndpointJobId(long endpointJobId);

    long countMetadataRecordByEndpointJobId(long endPointJobId);

    @Query(value = "SELECT count(metadata_record.record_identifier)  " +
            " FROM metadata_record " +
            " WHERE endpoint_job_id IN (SELECT endpoint_job_id FROM endpoint_job WHERE harvest_job_id = ?1)",
            nativeQuery = true)
    long countAllRecordIdentifier(String jobId);

    @Query(value = "SELECT metadata_record.record_identifier  " +
            " FROM metadata_record " +
            " WHERE endpoint_job_id IN (SELECT endpoint_job_id FROM endpoint_job WHERE harvest_job_id = ?1)",
    nativeQuery = true)
    List<String> findAllRecordIdentifier(String jobId);

    @Query("select new com.geocat.ingester.model.harvester.MetadataRecordXml(r.recordIdentifier, r.sha2, b.textValue) from MetadataRecord r join BlobStorage b ON r.sha2 = b.sha2 where r.endpointJobId = :endpointJobId order by r.metadataRecordId")
    Page<MetadataRecordXml> findMetadataRecordWithXmlByEndpointJobId(@Param("endpointJobId") long endpointJobId, Pageable pageable);
}
