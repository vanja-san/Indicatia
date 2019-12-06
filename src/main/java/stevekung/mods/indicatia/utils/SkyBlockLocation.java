package stevekung.mods.indicatia.utils;

public enum SkyBlockLocation
{
    NONE("NONE"),
    YOUR_ISLAND("Your Island"),

    // Public Island
    VILLAGE("Village"),
    FARM("Farm"),
    COAL_MINE("Coal Mine"),
    FOREST("Forest"),
    RUINS("Ruins"),
    MOUNTAIN("Mountain"),
    HIGH_LEVEL("High Level"),
    GRAVEYARD("Graveyard"),
    WILDERNESS("Wilderness"),
    FISHERMAN_HUT("Fisherman's Hut"),
    COLOSSEUM("Colosseum"),

    // Public Island building
    FLOWER_HOUSE("Flower House"),
    LIBRARY("Library"),
    BANK("Bank"),
    AUCTION_HOUSE("Auction House"),
    TAVERN("Tavern"),
    FASHION_SHOP("Fashion Shop"),

    BLAZING_FORTRESS("Blazing Fortress"),

    SPIDER_DEN("Spider's Den"),

    THE_END("The End"),
    DRAGON_NEST("Dragon's Nest"),
    ;

    private final String location;

    private SkyBlockLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return this.location;
    }

    public boolean isHub()
    {
        return this == VILLAGE || this == FARM || this == COAL_MINE || this == FOREST || this == RUINS || this == MOUNTAIN || this == HIGH_LEVEL || this == WILDERNESS || this == FISHERMAN_HUT || this == COLOSSEUM || this == GRAVEYARD || this.isInsideBuildingOnHub();
    }

    public boolean isInsideBuildingOnHub()
    {
        return this == FLOWER_HOUSE || this == LIBRARY || this == BANK || this == AUCTION_HOUSE || this == TAVERN || this == FASHION_SHOP;
    }

    public boolean isTheEnd()
    {
        return this == THE_END || this == DRAGON_NEST;
    }
}