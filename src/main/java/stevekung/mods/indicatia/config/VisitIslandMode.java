package stevekung.mods.indicatia.config;

public enum VisitIslandMode
{
    CHAT, TOAST, DISABLED;

    private static final VisitIslandMode[] values = values();

    public static String getById(int mode)
    {
        return values[mode].toString().toLowerCase();
    }
}