package stevekung.mods.indicatia.config;

public enum VisitIslandMode
{
    CHAT, TOAST, CHAT_AND_TOAST, DISABLED;

    private static final VisitIslandMode[] values = values();

    public static String getById(int mode)
    {
        return values[mode].toString().toLowerCase();
    }
}