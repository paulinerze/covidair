package com.miage.covidair.model.Location;

import com.google.gson.annotations.Expose;
import com.miage.covidair.model.Location.Loca;

import java.util.List;

public class LocationSearchResult {
    @Expose
    public List<Loca> results;

    public LocationSearchResult(List<Loca> results){
        this.results = results;
    }
}
