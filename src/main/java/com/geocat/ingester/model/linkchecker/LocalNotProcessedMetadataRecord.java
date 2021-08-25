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

package com.geocat.ingester.model.linkchecker;

import com.geocat.ingester.model.linkchecker.helper.MetadataRecord;
import com.geocat.ingester.model.linkchecker.helper.ServiceMetadataDocumentState;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


@Entity
@DiscriminatorValue("NoProcessedMetadataRecord")
public class LocalNotProcessedMetadataRecord extends MetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long localNotProcessedMetadataRecordId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    ServiceMetadataDocumentState state;

    @Column(columnDefinition = "varchar(40)")
    private String linkCheckJobId;

    private long harvesterMetadataRecordId;

    @Column(columnDefinition = "text")
    private String summary;


    public long getLocalNotProcessedMetadataRecordId() {
        return localNotProcessedMetadataRecordId;
    }

    public void setLocalNotProcessedMetadataRecordId(long localNotProcessedMetadataRecordId) {
        this.localNotProcessedMetadataRecordId = localNotProcessedMetadataRecordId;
    }

    public String getLinkCheckJobId() {
        return linkCheckJobId;
    }

    public void setLinkCheckJobId(String linkCheckJobId) {
        this.linkCheckJobId = linkCheckJobId;
    }


    public long getHarvesterMetadataRecordId() {
        return harvesterMetadataRecordId;
    }

    public void setHarvesterMetadataRecordId(long harvesterMetadataRecordId) {
        this.harvesterMetadataRecordId = harvesterMetadataRecordId;
    }


    public ServiceMetadataDocumentState getState() {
        return state;
    }

    public void setState(ServiceMetadataDocumentState state) {
        this.state = state;
    }


    //---------------------------------------------------------------------------

    @PreUpdate
    protected void onUpdate() {
        summary = toString();

    }

    @PrePersist
    protected void onInsert() {
        summary = toString();

    }

    //---------------------------------------------------------------------------

    @Override
    public String toString() {
        String result = "LocalNotProcessedMetadataRecord {\n";
        result += "      NOT PROCESSED Metadata Document Id: " + localNotProcessedMetadataRecordId+ "\n";
        result += "     linkCheckJobId: " + linkCheckJobId + "\n";
        result += "     harvesterMetadataRecordId: " + harvesterMetadataRecordId + "\n";
        result += "     state: " + state + "\n";


        result += super.toString();

        result += "\n";

        result += " }";
        return result;
    }
}