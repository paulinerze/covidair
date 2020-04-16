package com.miage.covidair.model.Location;

import com.google.gson.annotations.Expose;

import java.util.List;

public class LatestSearchResult {
    @Expose
    public List<Latest> results;

    public LatestSearchResult(List<Latest> results){
        this.results = results;
    }
}
