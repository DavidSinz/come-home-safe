package com.example.moni.comehomesafe;

public class PlacesItem {

    private String place;
    private String street;
    private String number;
    private String zipCode;
    private String city;

    public PlacesItem(String place, String street, String number, String zipCode, String city) {
        this.place = place;
        this.street = street;
        this.number = number;
        this.zipCode = zipCode;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getPlace() {
        return place;
    }
}
