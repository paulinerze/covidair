package com.miage.covidair.model;

public class Detail {
    private String parameter;
    private String value;
    private String unit;
    private String localDate;
    private String longitude;
    private String latitude;

    public Detail(String parameter, String value, String unit, String localDate, String longitude, String latitude) {
        this.parameter = parameter;
        this.value = value;
        this.unit = unit;
        this.localDate = localDate;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
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
}
