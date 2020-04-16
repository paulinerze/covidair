package com.miage.covidair.model.City;

import com.google.gson.annotations.Expose;

import java.util.List;

public class CitySearchResult {
    @Expose
    public List<City> results;

    public CitySearchResult(List<City> results){
        this.results = results;
    }
}
