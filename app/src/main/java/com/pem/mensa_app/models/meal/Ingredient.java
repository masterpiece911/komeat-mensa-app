package com.pem.mensa_app.models.meal;

public enum Ingredient {

    GQB("Certified Quality - Bavaria"),
    MSC("Marine Stewardship Council"),
    _1("with dyestuff"),
    _2("with preservative"),
    _3("with antioxidant"),
    _4("with flavor enhancers"),
    _5("sulphured"),
    _6("blackened (olive)"),
    _7("waxed"),
    _8("with phosphate"),
    _9("with sweeteners"),
    _10("contains a source of phenylalanine"),
    _11("with sugar and sweeteners"),
    _13("with cocoa-containing grease"),
    _14("with gelatin"),
    _99("with alcohol"),
    f("meatless dish"),
    v("vegan dish"),
    S("with pork"),
    R("with beef"),
    K("with veal"),
    G("with poultry"), // mediziner mensa
    W("with wild meat"), // mediziner mensa
    L("with lamb"), // mediziner mensa
    Kn("with garlic"),
    Ei("with chicken egg"),
    En("with peanut"),
    Fi("with fish"),
    Gl("with gluten-containing cereals"),
    GlW("with wheat"),
    GlR("with rye"),
    GlG("with barley"),
    GlH("with oats"),
    GlD("with spelt"),
    Kr("with crustaceans"),
    Lu("with lupines"),
    Mi("with milk and lactose"),
    Sc("with shell fruits"),
    ScM("with almonds"),
    ScH("with hazelnuts"),
    ScW("with Walnuts"),
    ScC("with cashew nuts"),
    ScP("with pistachios"),
    Se("with sesame seeds"),
    Sf("with mustard"),
    Sl("with celery"),
    So("with soy"),
    Sw("with sulfur dioxide and sulfites"),
    Wt("with mollusks");

    private final String description;

    Ingredient(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.getDescription();
    }
}
