package com.miage.covidair.model.Location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.miage.covidair.model.Measurement.Measurement;

import java.util.HashMap;

@Table(name = "Loca")
public class Loca extends Model {

    @Expose
    @Column(name = "location", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String location;
    @Expose
    @Column(name = "count")
    public String count;
    @Expose
    @Column(name = "lastUpdated")
    public String lastUpdated;
    @Expose
    @Column(name = "city")
    public String city;
    @Expose
    public Coordinates coordinates;
    @Expose
    @Column(name = "longitude")
    public String longitude;
    @Expose
    @Column(name = "latitude")
    public String latitude;
    @Expose
    @Column(name = "latestMeasurements")
    public HashMap<String, Measurement> latestMeasurements;
    @Expose
    @Column(name = "sol")
    public Double sol;
    @Expose
    @Column(name = "vent")
    public Double vent;
    @Expose
    @Column(name = "pluie")
    public Double pluie;

    public void setLatestMeasurements(HashMap<String, Measurement> latestMeasurements) {
        this.latestMeasurements = latestMeasurements;
    }

    public HashMap<String,Measurement> getLatestMeasurements() {
        return latestMeasurements;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Double getSol() {
        return sol;
    }

    public void setSol(Double sol) {
        this.sol = sol;
    }
}
