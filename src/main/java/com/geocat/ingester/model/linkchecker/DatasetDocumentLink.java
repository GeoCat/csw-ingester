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

import com.geocat.ingester.model.linkchecker.helper.DatasetMetadataRecord;
import com.geocat.ingester.model.linkchecker.helper.DocumentLink;
import com.geocat.ingester.model.linkchecker.helper.PartialDownloadHint;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


@Entity
public class DatasetDocumentLink extends DocumentLink {


    @ManyToOne(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
//    @JoinColumn(name="serviceMetadataId")
    DatasetMetadataRecord datasetMetadataRecord;
    @Column(columnDefinition = "text")
    String summary;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long datasetMetadataLinkId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "capabilitiesDocumentId")
    private CapabilitiesDocument capabilitiesDocument;



    //---------------------------------------------------------------------------

    public DatasetDocumentLink() {
        super();
        this.setPartialDownloadHint(PartialDownloadHint.CAPABILITIES_ONLY);
    }


    //---------------------------------------------------------------------------


    public DatasetMetadataRecord getDatasetMetadataRecord() {
        return datasetMetadataRecord;
    }

    public void setDatasetMetadataRecord(DatasetMetadataRecord datasetMetadataRecord) {
        this.datasetMetadataRecord = datasetMetadataRecord;
    }

    public long getDatasetMetadataLinkId() {
        return datasetMetadataLinkId;
    }

    public void setDatasetMetadataLinkId(long datasetMetadataLinkId) {
        this.datasetMetadataLinkId = datasetMetadataLinkId;
    }

    public CapabilitiesDocument getCapabilitiesDocument() {
        return capabilitiesDocument;
    }

    public void setCapabilitiesDocument(CapabilitiesDocument capabilitiesDocument) {
        this.capabilitiesDocument = capabilitiesDocument;
    }

    //---------------------------------------------------------------------------

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        this.summary = this.toString();
    }

    @PrePersist
    protected void onInsert() {
        super.onInsert();
        this.summary = this.toString();
    }

    //---------------------------------------------------------------------------


    @Override
    public String toString() {
        String result = "DatasetDocumentLink {\n";
        result += "      datasetMetadataLinkId: " + datasetMetadataLinkId + "\n";


//        if ( (serviceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord record identifier: "+ serviceMetadataRecord.getFileIdentifier()+"\n";
//        if ( (serviceMetadataRecord != null)   )
//            result += "      serviceMetadataRecord Id: "+ serviceMetadataRecord.getServiceMetadataDocumentId()+"\n";

        result += "\n";
        result += super.toString();
        result += "\n";
        result += "     +  Link is Capabilities Document: " + (getCapabilitiesDocument() != null) + "\n";
//        if (getCapabilitiesDocument() != null) {
//            result += getCapabilitiesDocument().toString(8);
//        }

        result += "\n";

        result += "  }";
        return result;
    }
}