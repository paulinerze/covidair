package com.miage.covidair.event;


import com.miage.covidair.model.City.City;

import java.util.List;

public class SearchCityResultEvent {

    private List<City> cities;

    public SearchCityResultEvent(List<City> cities) {
        this.cities = cities;
    }

    public List<City> getCities() {
        return cities;
    }
}
