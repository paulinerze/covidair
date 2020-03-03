package com.miage.covidair.event;

import com.miage.covidair.model.City;

import java.util.List;

public class SearchResultEvent {

    private List<City> cities;

    public SearchResultEvent(List<City> cities) {
        this.cities = cities;
    }

    public List<City> getCities() {
        return cities;
    }
}
