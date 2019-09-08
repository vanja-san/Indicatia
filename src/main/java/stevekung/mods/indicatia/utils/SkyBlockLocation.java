package stevekung.mods.indicatia.utils;

public enum SkyBlockLocation
{
    NONE("NONE"),
    YOUR_ISLAND("Your Island"),
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
    BLAZING_FORTRESS("Blazing Fortress"),
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

    public boolean isPublicIsland()
    {
        return this == VILLAGE || this == FARM || this == COAL_MINE || this == FOREST || this == RUINS || this == MOUNTAIN || this == HIGH_LEVEL || this == WILDERNESS || this == FISHERMAN_HUT || this == COLOSSEUM || this == GRAVEYARD;
    }
}