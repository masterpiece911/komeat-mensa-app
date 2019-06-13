package com.pem.mensa_app.models.mensa;

public enum Occupancy{
    UNAVAILABLE(0),
    GREEN_LOW(1),
    GREEN_HIGH(2),
    YELLOW_LOW(3),
    YELLOW_HIGH(4),
    RED(5);

    private int occupancy;

    Occupancy(int occupancyString){
        this.occupancy = occupancyString;
    }

    public int toInt() {
        return occupancy;
    }

    public static Occupancy fromInt(int level) {
        switch(level){
            case 0: return Occupancy.UNAVAILABLE;
            case 1: return Occupancy.GREEN_LOW;
            case 2: return Occupancy.GREEN_HIGH;
            case 3: return Occupancy.YELLOW_LOW;
            case 4: return Occupancy.YELLOW_HIGH;
            case 5: return Occupancy.RED;
            default: throw new IllegalArgumentException("invalid occupancy level");
        }
    }

    public static Occupancy fromDouble(double level) {
        return fromInt((int) level);
    }
}