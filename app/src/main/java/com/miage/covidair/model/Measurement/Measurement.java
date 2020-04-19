package com.miage.covidair.model.Measurement;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.miage.covidair.model.Location.Coordinates;

import java.util.Date;

@Table(name = "Measurement")
public class Measurement extends Model {
    @Expose
    @Column(name = "location", index = true, unique = false, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String location;
    @Expose
    @Column(name = "city")
    public String city;
    @Expose
    @Column(name = "parameter")
    public String parameter;
    @Expose
    @Column(name = "value")
    public String value;
    @Expose
    @Column(name = "unit")
    public String unit;
    @Expose
    @Column(name = "latitude")
    public String latitude;
    @Expose
    @Column(name = "longitude")
    public String longitude;
    @Expose
    @Column(name = "date")
    public String utcDate;
    @Expose
    public MeasurementDate date;
    @Expose
    public Coordinates coordinates;

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

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUtcDate() {
        return utcDate;
    }

    public void setUtcDate(String utcDate) {
        this.utcDate = utcDate;
    }
}
