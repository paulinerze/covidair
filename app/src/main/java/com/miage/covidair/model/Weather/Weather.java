package com.miage.covidair.model.Weather;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.Date;

@Table(name = "weather")
public class Weather extends Model {
    @Expose
    @Column(name = "date", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public Date date;
    @Expose
    @Column(name = "location")
    public String location;
    @Expose
    @Column(name = "city")
    public String city;
    @Expose
    @Column(name = "sol")
    public Double sol;
    @Expose
    @Column(name = "twom")
    public Double twoM;

    public Weather(Date date, String location, String city, Double sol, Double twoM) {
        this.date = date;
        this.location = location;
        this.city = city;
        this.sol = sol;
        this.twoM = twoM;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getSol() {
        return sol;
    }

    public void setSol(Double sol) {
        this.sol = sol;
    }

    public Double getTwoM() {
        return twoM;
    }

    public void setTwoM(Double twoM) {
        this.twoM = twoM;
    }
}
