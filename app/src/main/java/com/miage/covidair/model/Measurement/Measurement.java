package com.miage.covidair.model.Measurement;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.Date;

@Table(name = "Measurement")
public class Measurement extends Model {
    @Expose
    @Column(name = "key", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String key;
    @Expose
    @Column(name = "location")
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
    @Column(name = "displayDate")
    public String displayDate;
    @Expose
    @Column(name = "orderDate")
    public Date orderDate;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
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

}
