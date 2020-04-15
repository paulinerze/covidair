package com.miage.covidair.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class CitySearchResult {
    @Expose
    public List<City> results;
}
