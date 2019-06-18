package com.pem.mensa_app.models.mensa;

import java.util.Objects;

public class Mensa {

    private String uID;
    private String name;
    private String address;
    private String url;
    private Occupancy occupancy;
    private RestaurantType type;
    private VisibilityPreference visibility;
    private double latitude;
    private double longitude;
    private double distance = -1;

    public Mensa(String uID, String name, String address, Occupancy occupancy, RestaurantType type, double latitude, double longitude) {
        this.uID = uID;
        this.name = name;
        this.address = address;
        this.occupancy = occupancy;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Mensa() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mensa mensa = (Mensa) o;
        return uID.equals(mensa.uID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uID);
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public VisibilityPreference getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityPreference visibility) {
        this.visibility = visibility;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
