package com.geocat.ingester.model.harvester;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MetadataRecordXml {

    private String recordIdentifier;
    private String sha2;
    private String textValue;
    private Map<String, String> indicators;

    public MetadataRecordXml(String recordIdentifier, String sha2, String textValue) {
        this.recordIdentifier = recordIdentifier;
        this.sha2 = sha2;
        this.textValue = textValue;
        this.indicators = new HashMap<>();
    }


    public void addIndicator(String name, String value) {
        indicators.put(name, value);
    }

    public String getIndicatorValue(String name) {
        if (indicators.containsKey(name)) {
            return indicators.get(name);
        } else {
            return "";
        }
    }

    /*public String getMetadataRecordId() {
        return metadataRecordId;
    }

    public void setMetadataRecordId(String metadataRecordId) {
        this.metadataRecordId = metadataRecordId;
    }

    public String getSha2() {
        return sha2;
    }

    public void setSha2(String sha2) {
        this.sha2 = sha2;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }*/
}
