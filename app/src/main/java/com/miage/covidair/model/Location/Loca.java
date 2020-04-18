package com.miage.covidair.model.Location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    @Column(name = "measurements")
    public HashMap<String,Measurement> measurements;

    public void setMeasurements(HashMap<String, Measurement> measurements) {
        this.measurements = measurements;
    }

    public HashMap<String,Measurement> getMeasurements() {
        return measurements;
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

}
