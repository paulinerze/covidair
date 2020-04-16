package com.miage.covidair.event;

import com.miage.covidair.model.Location.Loca;


import java.util.List;

public class SearchLocationResultEvent {

    private List<Loca> locations;

    public SearchLocationResultEvent(List<Loca> locations) {
        this.locations = locations;
    }

    public List<Loca> getLocations() {
        return locations;
    }
}

