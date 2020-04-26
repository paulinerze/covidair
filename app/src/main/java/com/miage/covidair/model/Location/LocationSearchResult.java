package com.miage.covidair.model.Location;

import com.google.gson.annotations.Expose;

import java.util.List;

public class LocationSearchResult {
    @Expose
    public List<Location> results;

    public LocationSearchResult(List<Location> results){
        this.results = results;
    }
}
