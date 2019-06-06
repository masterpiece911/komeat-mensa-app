package com.pem.mensa_app.models.mensa;

public class Mensa {

    private String name;
    private String address;
    private Occupancy occupancy;
    private RestaurantType type;
    private double latitude;
    private double longitude;

    public Mensa(String name, String address, Occupancy occupancy, RestaurantType type, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.occupancy = occupancy;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(Occupancy occupancy) {
        this.occupancy = occupancy;
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
