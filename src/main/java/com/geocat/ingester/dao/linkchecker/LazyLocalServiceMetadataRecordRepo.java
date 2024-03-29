/*
 *  =============================================================================
 *  ===  Copyright (C) 2021 Food and Agriculture Organization of the
 *  ===  United Nations (FAO-UN), United Nations World Food Programme (WFP)
 *  ===  and United Nations Environment Programme (UNEP)
 *  ===
 *  ===  This program is free software; you can redistribute it and/or modify
 *  ===  it under the terms of the GNU General Public License as published by
 *  ===  the Free Software Foundation; either version 2 of the License, or (at
 *  ===  your option) any later version.
 *  ===
 *  ===  This program is distributed in the hope that it will be useful, but
 *  ===  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  ===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  ===  General Public License for more details.
 *  ===
 *  ===  You should have received a copy of the GNU General Public License
 *  ===  along with this program; if not, write to the Free Software
 *  ===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *  ===
 *  ===  Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 *  ===  Rome - Italy. email: geonetwork@osgeo.org
 *  ===
 *  ===  Development of this program was financed by the European Union within
 *  ===  Service Contract NUMBER – 941143 – IPR – 2021 with subject matter
 *  ===  "Facilitating a sustainable evolution and maintenance of the INSPIRE
 *  ===  Geoportal", performed in the period 2021-2023.
 *  ===
 *  ===  Contact: JRC Unit B.6 Digital Economy, Via Enrico Fermi 2749,
 *  ===  21027 Ispra, Italy. email: JRC-INSPIRE-SUPPORT@ec.europa.eu
 *  ==============================================================================
 */

package com.geocat.ingester.dao.linkchecker;

import com.geocat.ingester.model.linkchecker.LocalServiceMetadataRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Component
public class LazyLocalServiceMetadataRecordRepo {

//    @Autowired
//    LocalServiceMetadataRecordRepo localServiceMetadataRecordRepo;

    @Autowired
    @Qualifier("linkcheckerEntityManager")
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Autowired
    //@Qualifier("transactionManager")
    PlatformTransactionManager transactionManager;



    // replaces List<LocalServiceMetadataRecord> findAllByFileIdentifierAndLinkCheckJobId
    // Only return the first instance (code was always using .get(0) )
    // Only return the most basic version of the LocalServiceMetadataRecord (no operatesOn and no documentLinks).
    // DO NOT SAVE THESE OBJECTS
    // DO NOT ACCESS operateOn or documentLinks  (hibernate session is closed)
    public synchronized   Optional<LocalServiceMetadataRecord>  searchFirstByFileIdentifierAndLinkCheckJobId(String fileIdentifier, String linkCheckJobId){


        EntityManager  entityManager =  localContainerEntityManagerFactoryBean.createNativeEntityManager(null);
        try {

            EntityGraph entityGraph = entityManager.getEntityGraph("LocalServiceMetadataRecord-lazy-graph");


            Query query = entityManager.createQuery("SELECT lsmr FROM LocalServiceMetadataRecord lsmr WHERE lsmr.linkCheckJobId = :linkCheckJobId AND lsmr.fileIdentifier = :fileIdentifier")
                    .setHint("javax.persistence.fetchgraph", entityGraph)
                    .setParameter("linkCheckJobId", linkCheckJobId)
                    .setParameter("fileIdentifier", fileIdentifier)
                    .setMaxResults(1);
            List results = query.getResultList();

            Optional<LocalServiceMetadataRecord> result = Optional.empty();
            if (results.size() != 0) {
                result = Optional.of((LocalServiceMetadataRecord) results.get(0));
            }

            return result;
        }
        finally {
            entityManager.clear();
            entityManager.close();
        }
    }


}
