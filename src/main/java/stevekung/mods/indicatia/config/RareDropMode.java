package stevekung.mods.indicatia.config;

public enum RareDropMode
{
    CHAT, TOAST;

    private static final RareDropMode[] values = values();

    public static String getById(int mode)
    {
        return values[mode].toString().toLowerCase();
    }
}