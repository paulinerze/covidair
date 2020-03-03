package com.miage.covidair.model;

public class City {

    private String name;
    private String count;
    private String locations;

    public City(String name, String count, String locations) {
        this.name = name;
        this.count = count;
        this.locations = locations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }
}
