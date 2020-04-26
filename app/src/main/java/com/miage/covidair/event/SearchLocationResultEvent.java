package com.miage.covidair.event;

import com.miage.covidair.model.Location.Location;

import java.util.List;

public class SearchLocationResultEvent {

    private List<Location> locations;

    public SearchLocationResultEvent(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() {
        return locations;
    }
}

