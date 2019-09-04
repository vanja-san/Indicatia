package stevekung.mods.indicatia.utils;

public enum SkyBlockLocation
{
    YOUR_ISLAND("Your Island"),
    VILLAGE("Village"),
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
}