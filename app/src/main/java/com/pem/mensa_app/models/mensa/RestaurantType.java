package com.pem.mensa_app.models.mensa;

public enum RestaurantType{
    MENSA("mensa", "#76AD40"),
    STUCAFE("stucafe", "#F18800"),
    STULOUNGE("stulounge", "#5D2F00"),
    STUBISTRO("stubistro", "#B90748");

    String typeName;
    String colorRGB;

    RestaurantType(String typeName, String colorRGB) {
        this.typeName = typeName;
        this.colorRGB = colorRGB;
    }

    public String toString() {
        return typeName;
    }

    public String toColor() {return colorRGB;}

    public static RestaurantType fromString(String typeName) {
        switch(typeName.toLowerCase()){
            case "mensa": return RestaurantType.MENSA;
            case "stucafe": return RestaurantType.STUCAFE;
            case "stulounge": return RestaurantType.STULOUNGE;
            case "stubistro": return RestaurantType.STUBISTRO;
            default: throw new IllegalArgumentException("invalid typename");
        }
    }
}