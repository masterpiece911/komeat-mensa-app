package com.pem.mensa_app.models.mensa;

public enum Occupancy{
    GREEN_LOW("1"),
    GREEN_HIGH("2"),
    YELLOW_LOW("3"),
    YELLOW_HIGH("4"),
    RED("5");

    private String occupancyString;

    Occupancy(String occupancyString){
        this.occupancyString = occupancyString;
    }

    public String getOccupancyString() {
        return occupancyString;
    }

    public static Occupancy fromInt(int level) {
        switch(level){
            case 1: return Occupancy.GREEN_LOW;
            case 2: return Occupancy.GREEN_HIGH;
            case 3: return Occupancy.YELLOW_LOW;
            case 4: return Occupancy.YELLOW_HIGH;
            case 5: return Occupancy.RED;
            default: throw new IllegalArgumentException("invalid occupancy level");
        }
    }
}