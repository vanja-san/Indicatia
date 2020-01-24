package stevekung.mods.indicatia.utils;

import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.EnumChatFormatting;
import stevekung.mods.indicatia.event.HypixelEventHandler;

public class SkyBlockBossStatus
{
    public static void setBossStatus(IBossDisplayData displayData, boolean hasColorModifier)
    {
        String name = EnumChatFormatting.getTextWithoutFormattingCodes(displayData.getDisplayName().getUnformattedText());
        DragonType type = DragonType.SUPERIOR;

        if (name.equals("Old Dragon"))
        {
            type = DragonType.OLD;
        }
        else if (name.equals("Protector Dragon"))
        {
            type = DragonType.PROTECTOR;
        }
        else if (name.equals("Strong Dragon"))
        {
            type = DragonType.STRONG;
        }
        else if (name.equals("Superior Dragon"))
        {
            type = DragonType.SUPERIOR;
        }
        else if (name.equals("Unstable Dragon"))
        {
            type = DragonType.UNSTABLE;
        }
        else if (name.equals("Wise Dragon"))
        {
            type = DragonType.WISE;
        }
        else if (name.equals("Young Dragon"))
        {
            type = DragonType.YOUNG;
        }

        BossStatus.healthScale = HypixelEventHandler.dragonHealth / type.getMaxHealth();
        BossStatus.statusBarTime = 100;
        BossStatus.bossName = displayData.getDisplayName().getFormattedText();
        BossStatus.hasColorModifier = hasColorModifier;
    }

    enum DragonType
    {
        OLD(10000000),
        PROTECTOR(7500000),
        STRONG(7500000),
        SUPERIOR(10000000),
        UNSTABLE(6000000),
        WISE(6000000),
        YOUNG(5000000);

        private final float maxHealth;

        private DragonType(float maxHealth)
        {
            this.maxHealth = maxHealth;
        }

        public float getMaxHealth()
        {
            return this.maxHealth;
        }
    }
}