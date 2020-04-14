package com.miage.covidair.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "City")
public class City extends Model {

    @Column(name = "name", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String name;
    @Column(name = "count")
    public String count;
    @Column(name = "locations")
    public String locations;

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
