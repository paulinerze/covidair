package com.miage.covidair.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "City")
public class City extends Model {

    @Expose
    @Column(name = "name", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String name;
    @Expose
    @Column(name = "count")
    public String count;
    @Expose
    @Column(name = "locations")
    public String locations;

    public City() {
        super();
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
