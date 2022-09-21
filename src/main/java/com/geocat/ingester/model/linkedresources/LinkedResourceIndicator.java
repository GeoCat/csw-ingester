package com.geocat.ingester.model.linkedresources;

import java.util.Collections;
import java.util.List;

public class LinkedResourceIndicator {
    private String name;
    private String link;
    private List<String> associatedServiceIds;

    public LinkedResourceIndicator(String name, String link, List<String> associatedServiceIds) {
        this.name = name;
        this.link = link;
        this.associatedServiceIds = associatedServiceIds;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public List<String> getAssociatedServiceIds() {
        return Collections.unmodifiableList(associatedServiceIds);
    }
}
