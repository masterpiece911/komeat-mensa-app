package com.pem.mensa_app.models;

public class Mensa {

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

    public enum RestaurantType{
        MENSA("mensa"),
        STUCAFE("stucafe"),
        STULOUNGE("stulounge"),
        STUBISTRO("stubistro");

        String typeName;

        RestaurantType(String typeName) {
            this.typeName = typeName;
        }

        public static RestaurantType fromString(String typeName) {
            switch(typeName){
                case "mensa": return RestaurantType.MENSA;
                case "stucafe": return RestaurantType.STUCAFE;
                case "stulounge": return RestaurantType.STULOUNGE;
                case "stubistro": return RestaurantType.STUBISTRO;
                default: throw new IllegalArgumentException("invalid typename");
            }
        }


    }

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

}
