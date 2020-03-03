package com.miage.covidair.model;

public class Location {

    private String location;
    private String count;
    private String lastUpdated;

    public Location(String location, String count, String lastUpdated) {
        this.location = location;
        this.count = count;
        this.lastUpdated = lastUpdated;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
