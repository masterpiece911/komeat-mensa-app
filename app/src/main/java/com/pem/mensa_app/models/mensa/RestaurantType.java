package com.pem.mensa_app.models.mensa;

public enum RestaurantType{
    MENSA("mensa"),
    STUCAFE("stucafe"),
    STULOUNGE("stulounge"),
    STUBISTRO("stubistro");

    String typeName;

    RestaurantType(String typeName) {
        this.typeName = typeName;
    }

    public String toString() {
        return typeName;
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