package stevekung.mods.indicatia.config;

import java.io.*;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevekung.mods.indicatia.gui.config.GuiExtendedConfig;
import stevekung.mods.indicatia.utils.ClientUtils;
import stevekung.mods.indicatia.utils.GameProfileUtils;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.LoggerIN;

public class ExtendedConfig
{
    public static ExtendedConfig instance = new ExtendedConfig();
    private static final String defaultWhite = "255,255,255";
    public static final File indicatiaDir = new File(Minecraft.getMinecraft().mcDataDir, "indicatia");
    public static final File userDir = new File(indicatiaDir, GameProfileUtils.getUUID().toString());
    public static final File defaultConfig = new File(userDir, "default.dat");
    public static String currentProfile = "";
    private static final String[] HEALTH_STATUS = new String[] {"indicatia.disabled", "health_status.always", "health_status.pointed"};
    private static final String[] EQUIPMENT_ORDERING = new String[] {"indicatia.default", "equipment.reverse"};
    private static final String[] EQUIPMENT_DIRECTION = new String[] {"equipment.vertical", "equipment.horizontal"};
    private static final String[] EQUIPMENT_STATUS = new String[] {"equipment.damage_and_max_damage", "equipment.percent", "equipment.only_damage", "indicatia.none", "equipment.count", "equipment.count_and_stack"};
    private static final String[] EQUIPMENT_POSITION = new String[] {"indicatia.left", "indicatia.right", "indicatia.hotbar"};
    private static final String[] POTION_STATUS_HUD_STYLE = new String[] {"indicatia.default", "potion_hud.icon_and_time"};
    private static final String[] POTION_STATUS_HUD_POSITION = new String[] {"indicatia.left", "indicatia.right", "indicatia.hotbar_left", "indicatia.hotbar_right"};
    private static final String[] PING_MODE = new String[] {"indicatia.only_ping", "indicatia.ping_and_delay"};
    private static final String[] VISIT_ISLAND_MODE = new String[] {"indicatia.chat", "indicatia.toast", "indicatia.disabled"};
    private static final String[] RARE_DROP_MODE = new String[] {"indicatia.chat", "indicatia.toast"};
    private static File file;

    // Render Info
    public boolean fps = true;
    public boolean xyz = true;
    public boolean direction = true;
    public boolean biome = true;
    public boolean ping = true;
    public boolean pingToSecond = false;
    public boolean serverIP = false;
    public boolean serverIPMCVersion = false;
    public boolean equipmentHUD = false;
    public boolean potionHUD = false;
    public boolean realTime = true;
    public boolean gameTime = true;
    public boolean gameWeather = true;
    public boolean moonPhase = true;
    public boolean potionHUDIcon = false;
    public boolean alternatePotionHUDTextColor = false;
    public boolean toggleSprint = false;
    public boolean toggleSneak = false;

    // Main
    public boolean swapRenderInfo = false;
    public int healthStatusMode = 0;
    public int equipmentOrdering = 0;
    public int equipmentDirection = 0;
    public int equipmentStatus = 0;
    public int equipmentPosition = 2;
    public int potionHUDStyle = 0;
    public int potionHUDPosition = 0;
    public int pingMode = 0;

    // Offset
    public int armorHUDYOffset = 0;
    public int potionHUDYOffset = 0;
    public int maximumPotionDisplay = 2;
    public int potionLengthYOffset = 23;
    public int potionLengthYOffsetOverlap = 45;

    // Custom Color
    public String fpsColor = defaultWhite;
    public String xyzColor = defaultWhite;
    public String biomeColor = defaultWhite;
    public String directionColor = defaultWhite;
    public String pingColor = defaultWhite;
    public String pingToSecondColor = defaultWhite;
    public String serverIPColor = defaultWhite;
    public String equipmentStatusColor = defaultWhite;
    public String arrowCountColor = defaultWhite;
    public String realTimeColor = defaultWhite;
    public String gameTimeColor = defaultWhite;
    public String gameWeatherColor = defaultWhite;
    public String moonPhaseColor = defaultWhite;
    public String jungleAxeDelayColor = defaultWhite;
    public String grapplingHookDelayColor = defaultWhite;
    public String zealotRespawnDelayColor = defaultWhite;

    // Custom Color : Value
    public String fpsValueColor = "85,255,85";
    public String fps26And49Color = "255,255,85";
    public String fpsLow25Color = "255,85,85";
    public String xyzValueColor = defaultWhite;
    public String directionValueColor = defaultWhite;
    public String biomeValueColor = defaultWhite;
    public String pingValueColor = "85,255,85";
    public String ping200And300Color = "255,255,85";
    public String ping300And500Color = "255,85,85";
    public String pingMax500Color = "170,0,0";
    public String serverIPValueColor = defaultWhite;
    public String realTimeHHMMSSValueColor = defaultWhite;
    public String realTimeDDMMYYValueColor = defaultWhite;
    public String gameTimeValueColor = defaultWhite;
    public String gameWeatherValueColor = defaultWhite;
    public String moonPhaseValueColor = defaultWhite;

    // Misc
    public String toggleSprintUseMode = "command";
    public String toggleSneakUseMode = "command";

    // Hypixel
    public boolean rightClickToAddParty = false;
    public boolean addPartyVisitIsland = false;
    public boolean jungleAxeOverlay = true;
    public boolean grapplingHookOverlay = true;
    public boolean zealotRespawnOverlay = false;
    public boolean dragonSetGlowingEye = false;
    public String hypixelNickName = "";
    public int selectedHypixelMinigame = 0;
    public int hypixelMinigameScrollPos = 0;
    public int jungleAxeDelay = 2000;
    public int grapplingHookDelay = 2000;
    public int zealotRespawnDelay = 15000;
    public int visitIslandMode = 1;
    public int itemDropMode = 1;
    public int chatMode = 0;

    private ExtendedConfig() {}

    public void setCurrentProfile(String profileName)
    {
        ExtendedConfig.file = new File(userDir, profileName + ".dat");
        currentProfile = profileName;
    }

    public void load()
    {
        try
        {
            NBTTagCompound nbt = CompressedStreamTools.read(ExtendedConfig.file);

            if (nbt == null)
            {
                return;
            }

            // Render Info
            this.fps = ExtendedConfig.getBoolean(nbt, "FPS", this.fps);
            this.xyz = ExtendedConfig.getBoolean(nbt, "XYZ", this.xyz);
            this.direction = ExtendedConfig.getBoolean(nbt, "Direction", this.direction);
            this.biome = ExtendedConfig.getBoolean(nbt, "Biome", this.biome);
            this.ping = ExtendedConfig.getBoolean(nbt, "Ping", this.ping);
            this.pingToSecond = ExtendedConfig.getBoolean(nbt, "PingToSecond", this.pingToSecond);
            this.serverIP = ExtendedConfig.getBoolean(nbt, "ServerIP", this.serverIP);
            this.serverIPMCVersion = ExtendedConfig.getBoolean(nbt, "ServerIPMCVersion", this.serverIPMCVersion);
            this.equipmentHUD = ExtendedConfig.getBoolean(nbt, "EquipmentHUD", this.equipmentHUD);
            this.potionHUD = ExtendedConfig.getBoolean(nbt, "PotionHUD", this.potionHUD);
            this.realTime = ExtendedConfig.getBoolean(nbt, "RealTime", this.realTime);
            this.gameTime = ExtendedConfig.getBoolean(nbt, "GameTime", this.gameTime);
            this.gameWeather = ExtendedConfig.getBoolean(nbt, "GameWeather", this.gameWeather);
            this.moonPhase = ExtendedConfig.getBoolean(nbt, "MoonPhase", this.moonPhase);
            this.potionHUDIcon = ExtendedConfig.getBoolean(nbt, "PotionHUDIcon", this.potionHUDIcon);
            this.alternatePotionHUDTextColor = ExtendedConfig.getBoolean(nbt, "AlternatePotionHUDTextColor", this.alternatePotionHUDTextColor);

            // Main
            this.swapRenderInfo = ExtendedConfig.getBoolean(nbt, "SwapRenderInfo", this.swapRenderInfo);
            this.healthStatusMode = ExtendedConfig.getInteger(nbt, "HealthStatusMode", this.healthStatusMode);
            this.equipmentOrdering = ExtendedConfig.getInteger(nbt, "EquipmentOrdering", this.equipmentOrdering);
            this.equipmentDirection = ExtendedConfig.getInteger(nbt, "EquipmentDirection", this.equipmentDirection);
            this.equipmentStatus = ExtendedConfig.getInteger(nbt, "EquipmentStatus", this.equipmentStatus);
            this.equipmentPosition = ExtendedConfig.getInteger(nbt, "EquipmentPosition", this.equipmentPosition);
            this.potionHUDStyle = ExtendedConfig.getInteger(nbt, "PotionHUDStyle", this.potionHUDStyle);
            this.potionHUDPosition = ExtendedConfig.getInteger(nbt, "PotionHUDPosition", this.potionHUDPosition);
            this.pingMode = ExtendedConfig.getInteger(nbt, "PingMode", this.pingMode);
            this.visitIslandMode = ExtendedConfig.getInteger(nbt, "VisitIslandMode", this.visitIslandMode);
            this.itemDropMode = ExtendedConfig.getInteger(nbt, "ItemDropMode", this.itemDropMode);
            this.chatMode = ExtendedConfig.getInteger(nbt, "ChatMode", this.chatMode);

            // Movement
            this.toggleSprint = ExtendedConfig.getBoolean(nbt, "ToggleSprint", this.toggleSprint);
            this.toggleSneak = ExtendedConfig.getBoolean(nbt, "ToggleSneak", this.toggleSneak);

            // Offset
            this.armorHUDYOffset = ExtendedConfig.getInteger(nbt, "ArmorHUDYOffset", this.armorHUDYOffset);
            this.potionHUDYOffset = ExtendedConfig.getInteger(nbt, "PotionHUDYOffset", this.potionHUDYOffset);
            this.maximumPotionDisplay = ExtendedConfig.getInteger(nbt, "MaximumPotionDisplay", this.maximumPotionDisplay);
            this.potionLengthYOffset = ExtendedConfig.getInteger(nbt, "PotionLengthYOffset", this.potionLengthYOffset);
            this.potionLengthYOffsetOverlap = ExtendedConfig.getInteger(nbt, "PotionLengthYOffsetOverlap", this.potionLengthYOffsetOverlap);

            // Custom Color
            this.fpsColor = ExtendedConfig.getString(nbt, "FPSColor", this.fpsColor);
            this.xyzColor = ExtendedConfig.getString(nbt, "XYZColor", this.xyzColor);
            this.biomeColor = ExtendedConfig.getString(nbt, "BiomeColor", this.biomeColor);
            this.directionColor = ExtendedConfig.getString(nbt, "DirectionColor", this.directionColor);
            this.pingColor = ExtendedConfig.getString(nbt, "PingColor", this.pingColor);
            this.pingToSecondColor = ExtendedConfig.getString(nbt, "PingToSecondColor", this.pingToSecondColor);
            this.serverIPColor = ExtendedConfig.getString(nbt, "ServerIPColor", this.serverIPColor);
            this.equipmentStatusColor = ExtendedConfig.getString(nbt, "EquipmentStatusColor", this.equipmentStatusColor);
            this.arrowCountColor = ExtendedConfig.getString(nbt, "ArrowCountColor", this.arrowCountColor);
            this.realTimeColor = ExtendedConfig.getString(nbt, "RealTimeColor", this.realTimeColor);
            this.gameTimeColor = ExtendedConfig.getString(nbt, "GameTimeColor", this.gameTimeColor);
            this.gameWeatherColor = ExtendedConfig.getString(nbt, "GameWeatherColor", this.gameWeatherColor);
            this.moonPhaseColor = ExtendedConfig.getString(nbt, "MoonPhaseColor", this.moonPhaseColor);
            this.jungleAxeDelayColor = ExtendedConfig.getString(nbt, "JungleAxeDelayColor", this.jungleAxeDelayColor);
            this.grapplingHookDelayColor = ExtendedConfig.getString(nbt, "GrapplingHookDelayColor", this.grapplingHookDelayColor);
            this.zealotRespawnDelayColor = ExtendedConfig.getString(nbt, "ZealotRespawnDelayColor", this.zealotRespawnDelayColor);

            // Custom Color : Value
            this.fpsValueColor = ExtendedConfig.getString(nbt, "FPSValueColor", this.fpsValueColor);
            this.fps26And49Color = ExtendedConfig.getString(nbt, "FPS26And49Color", this.fps26And49Color);
            this.fpsLow25Color = ExtendedConfig.getString(nbt, "FPSLow25Color", this.fpsLow25Color);
            this.xyzValueColor = ExtendedConfig.getString(nbt, "XYZValueColor", this.xyzValueColor);
            this.biomeValueColor = ExtendedConfig.getString(nbt, "BiomeValueColor", this.biomeValueColor);
            this.directionValueColor = ExtendedConfig.getString(nbt, "DirectionValueColor", this.directionValueColor);
            this.pingValueColor = ExtendedConfig.getString(nbt, "PingValueColor", this.pingValueColor);
            this.ping200And300Color = ExtendedConfig.getString(nbt, "Ping200And300Color", this.ping200And300Color);
            this.ping300And500Color = ExtendedConfig.getString(nbt, "Ping300And500Color", this.ping300And500Color);
            this.pingMax500Color = ExtendedConfig.getString(nbt, "PingMax500Color", this.pingMax500Color);
            this.serverIPValueColor = ExtendedConfig.getString(nbt, "ServerIPValueColor", this.serverIPValueColor);
            this.realTimeHHMMSSValueColor = ExtendedConfig.getString(nbt, "RealTimeHHMMSSValueColor", this.realTimeHHMMSSValueColor);
            this.realTimeDDMMYYValueColor = ExtendedConfig.getString(nbt, "RealTimeDDMMYYValueColor", this.realTimeDDMMYYValueColor);
            this.gameTimeValueColor = ExtendedConfig.getString(nbt, "GameTimeValueColor", this.gameTimeValueColor);
            this.gameWeatherValueColor = ExtendedConfig.getString(nbt, "GameWeatherValueColor", this.gameWeatherValueColor);
            this.moonPhaseValueColor = ExtendedConfig.getString(nbt, "MoonPhaseValueColor", this.moonPhaseValueColor);

            // Misc
            this.toggleSprintUseMode = ExtendedConfig.getString(nbt, "ToggleSprintUseMode", this.toggleSprintUseMode);
            this.toggleSneakUseMode = ExtendedConfig.getString(nbt, "ToggleSneakUseMode", this.toggleSneakUseMode);

            // Hypixel
            this.rightClickToAddParty = ExtendedConfig.getBoolean(nbt, "RightClickToAddParty", this.rightClickToAddParty);
            this.addPartyVisitIsland = ExtendedConfig.getBoolean(nbt, "AddPartyVisitIsland", this.addPartyVisitIsland);
            this.hypixelNickName = ExtendedConfig.getString(nbt, "HypixelNickName", this.hypixelNickName);
            this.selectedHypixelMinigame = ExtendedConfig.getInteger(nbt, "SelectedHypixelMinigame", this.selectedHypixelMinigame);
            this.hypixelMinigameScrollPos = ExtendedConfig.getInteger(nbt, "HypixelMinigameScrollPos", this.hypixelMinigameScrollPos);
            this.jungleAxeDelay = ExtendedConfig.getInteger(nbt, "JungleAxeDelay", this.jungleAxeDelay);
            this.grapplingHookDelay = ExtendedConfig.getInteger(nbt, "GrapplingHookDelay", this.grapplingHookDelay);
            this.zealotRespawnDelay = ExtendedConfig.getInteger(nbt, "ZealotRespawnDelay", this.zealotRespawnDelay);
            this.jungleAxeOverlay = ExtendedConfig.getBoolean(nbt, "JungleAxeOverlay", this.jungleAxeOverlay);
            this.grapplingHookOverlay = ExtendedConfig.getBoolean(nbt, "GrapplingHookOverlay", this.grapplingHookOverlay);
            this.zealotRespawnOverlay = ExtendedConfig.getBoolean(nbt, "ZealotRespawnOverlay", this.zealotRespawnOverlay);
            this.dragonSetGlowingEye = ExtendedConfig.getBoolean(nbt, "DragonSetGlowingEye", this.dragonSetGlowingEye);

            LoggerIN.info("Loading extended config {}", ExtendedConfig.file.getPath());
        }
        catch (Exception e) {}
    }

    public void save()
    {
        this.save(!ExtendedConfig.currentProfile.isEmpty() ? ExtendedConfig.currentProfile : "default");
    }

    public void save(String profileName)
    {
        try
        {
            NBTTagCompound nbt = new NBTTagCompound();

            // Render Info
            nbt.setBoolean("FPS", this.fps);
            nbt.setBoolean("XYZ", this.xyz);
            nbt.setBoolean("Direction", this.direction);
            nbt.setBoolean("Biome", this.biome);
            nbt.setBoolean("Ping", this.ping);
            nbt.setBoolean("PingToSecond", this.pingToSecond);
            nbt.setBoolean("ServerIP", this.serverIP);
            nbt.setBoolean("ServerIPMCVersion", this.serverIPMCVersion);
            nbt.setBoolean("EquipmentHUD", this.equipmentHUD);
            nbt.setBoolean("PotionHUD", this.potionHUD);
            nbt.setBoolean("RealTime", this.realTime);
            nbt.setBoolean("GameTime", this.gameTime);
            nbt.setBoolean("GameWeather", this.gameWeather);
            nbt.setBoolean("MoonPhase", this.moonPhase);
            nbt.setBoolean("PotionHUDIcon", this.potionHUDIcon);
            nbt.setBoolean("AlternatePotionHUDTextColor", this.alternatePotionHUDTextColor);

            // Main
            nbt.setBoolean("SwapRenderInfo", this.swapRenderInfo);
            nbt.setInteger("HealthStatusMode", this.healthStatusMode);
            nbt.setInteger("EquipmentOrdering", this.equipmentOrdering);
            nbt.setInteger("EquipmentDirection", this.equipmentDirection);
            nbt.setInteger("EquipmentStatus", this.equipmentStatus);
            nbt.setInteger("EquipmentPosition", this.equipmentPosition);
            nbt.setInteger("PotionHUDStyle", this.potionHUDStyle);
            nbt.setInteger("PotionHUDPosition", this.potionHUDPosition);
            nbt.setInteger("PingMode", this.pingMode);
            nbt.setInteger("VisitIslandMode", this.visitIslandMode);
            nbt.setInteger("ItemDropMode", this.itemDropMode);
            nbt.setInteger("ChatMode", this.chatMode);

            // Movement
            nbt.setBoolean("ToggleSprint", this.toggleSprint);
            nbt.setBoolean("ToggleSneak", this.toggleSneak);

            // Offset
            nbt.setInteger("ArmorHUDYOffset", this.armorHUDYOffset);
            nbt.setInteger("PotionHUDYOffset", this.potionHUDYOffset);
            nbt.setInteger("MaximumPotionDisplay", this.maximumPotionDisplay);
            nbt.setInteger("PotionLengthYOffset", this.potionLengthYOffset);
            nbt.setInteger("PotionLengthYOffsetOverlap", this.potionLengthYOffsetOverlap);

            // Custom Color
            nbt.setString("FPSColor", this.fpsColor);
            nbt.setString("XYZColor", this.xyzColor);
            nbt.setString("BiomeColor", this.biomeColor);
            nbt.setString("DirectionColor", this.directionColor);
            nbt.setString("PingColor", this.pingColor);
            nbt.setString("PingToSecondColor", this.pingToSecondColor);
            nbt.setString("ServerIPColor", this.serverIPColor);
            nbt.setString("EquipmentStatusColor", this.equipmentStatusColor);
            nbt.setString("ArrowCountColor", this.arrowCountColor);
            nbt.setString("RealTimeColor", this.realTimeColor);
            nbt.setString("GameTimeColor", this.gameTimeColor);
            nbt.setString("GameWeatherColor", this.gameWeatherColor);
            nbt.setString("MoonPhaseColor", this.moonPhaseColor);
            nbt.setString("JungleAxeDelayColor", this.jungleAxeDelayColor);
            nbt.setString("GrapplingHookDelayColor", this.grapplingHookDelayColor);
            nbt.setString("ZealotRespawnDelayColor", this.zealotRespawnDelayColor);

            // Custom Color : Value
            nbt.setString("FPSValueColor", this.fpsValueColor);
            nbt.setString("FPS26And49Color", this.fps26And49Color);
            nbt.setString("FPSLow25Color", this.fpsLow25Color);
            nbt.setString("XYZValueColor", this.xyzValueColor);
            nbt.setString("BiomeValueColor", this.biomeValueColor);
            nbt.setString("DirectionValueColor", this.directionValueColor);
            nbt.setString("PingValueColor", this.pingValueColor);
            nbt.setString("Ping200And300Color", this.ping200And300Color);
            nbt.setString("Ping300And500Color", this.ping300And500Color);
            nbt.setString("PingMax500Color", this.pingMax500Color);
            nbt.setString("ServerIPValueColor", this.serverIPValueColor);
            nbt.setString("RealTimeHHMMSSValueColor", this.realTimeHHMMSSValueColor);
            nbt.setString("RealTimeDDMMYYValueColor", this.realTimeDDMMYYValueColor);
            nbt.setString("GameTimeValueColor", this.gameTimeValueColor);
            nbt.setString("GameWeatherValueColor", this.gameWeatherValueColor);
            nbt.setString("MoonPhaseValueColor", this.moonPhaseValueColor);

            // Misc
            nbt.setString("ToggleSprintUseMode", this.toggleSprintUseMode);
            nbt.setString("ToggleSneakUseMode", this.toggleSneakUseMode);

            // Hypixel
            nbt.setBoolean("RightClickToAddParty", this.rightClickToAddParty);
            nbt.setBoolean("AddPartyVisitIsland", this.addPartyVisitIsland);
            nbt.setBoolean("JungleAxeOverlay", this.jungleAxeOverlay);
            nbt.setBoolean("GrapplingHookOverlay", this.grapplingHookOverlay);
            nbt.setBoolean("ZealotRespawnOverlay", this.zealotRespawnOverlay);
            nbt.setBoolean("DragonSetGlowingEye", this.dragonSetGlowingEye);
            nbt.setString("HypixelNickName", this.hypixelNickName);
            nbt.setInteger("SelectedHypixelMinigame", this.selectedHypixelMinigame);
            nbt.setInteger("HypixelMinigameScrollPos", this.hypixelMinigameScrollPos);
            nbt.setInteger("JungleAxeDelay", this.jungleAxeDelay);
            nbt.setInteger("GrapplingHookDelay", this.grapplingHookDelay);
            nbt.setInteger("ZealotRespawnDelay", this.zealotRespawnDelay);

            CompressedStreamTools.safeWrite(nbt, !profileName.equalsIgnoreCase("default") ? new File(userDir, profileName + ".dat") : ExtendedConfig.file);
        }
        catch (Exception e) {}
    }

    public static void saveProfileFile(String profileName)
    {
        File profile = new File(ExtendedConfig.userDir, "profile.txt");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(profile), StandardCharsets.UTF_8)))
        {
            writer.println("profile:" + profileName);
            LoggerIN.info("Saving profile name!");
        }
        catch (IOException e)
        {
            LoggerIN.error("Failed to save profiles", (Throwable)e);
        }
    }

    public static void resetConfig()
    {
        ExtendedConfig.instance = new ExtendedConfig();
        ExtendedConfig.instance.save(ExtendedConfig.currentProfile);
        ClientUtils.printClientMessage(LangUtils.translate("message.reset_config", ExtendedConfig.currentProfile));
    }

    private static boolean getBoolean(NBTTagCompound nbt, String key, boolean defaultValue)
    {
        if (nbt.hasKey(key, 99))
        {
            return nbt.getBoolean(key);
        }
        else
        {
            return defaultValue;
        }
    }

    private static int getInteger(NBTTagCompound nbt, String key, int defaultValue)
    {
        if (nbt.hasKey(key, 99))
        {
            return nbt.getInteger(key);
        }
        else
        {
            return defaultValue;
        }
    }

    private static String getString(NBTTagCompound nbt, String key, String defaultValue)
    {
        if (nbt.hasKey(key, 8))
        {
            return nbt.getString(key);
        }
        else
        {
            return defaultValue;
        }
    }

    public String getKeyBinding(ExtendedConfig.Options options)
    {
        String name = LangUtils.translate(options.getTranslation()) + ": ";

        if (options.isFloat())
        {
            float value = this.getOptionFloatValue(options);
            return name + (int)value;
        }
        else if (options.isBoolean())
        {
            boolean flag = this.getOptionOrdinalValue(options);
            return flag ? name + EnumChatFormatting.GREEN + "true" : name + EnumChatFormatting.RED + "false";
        }
        else if (options.isTextbox())
        {
            return this.getOptionStringValue(options);
        }
        else if (options == ExtendedConfig.Options.HEALTH_STATUS)
        {
            return name + this.getTranslation(HEALTH_STATUS, this.healthStatusMode);
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_ORDERING)
        {
            return name + this.getTranslation(EQUIPMENT_ORDERING, this.equipmentOrdering);
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_DIRECTION)
        {
            return name + this.getTranslation(EQUIPMENT_DIRECTION, this.equipmentDirection);
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_STATUS)
        {
            return name + this.getTranslation(EQUIPMENT_STATUS, this.equipmentStatus);
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_POSITION)
        {
            return name + this.getTranslation(EQUIPMENT_POSITION, this.equipmentPosition);
        }
        else if (options == ExtendedConfig.Options.POTION_HUD_STYLE)
        {
            return name + this.getTranslation(POTION_STATUS_HUD_STYLE, this.potionHUDStyle);
        }
        else if (options == ExtendedConfig.Options.POTION_HUD_POSITION)
        {
            return name + this.getTranslation(POTION_STATUS_HUD_POSITION, this.potionHUDPosition);
        }
        else if (options == ExtendedConfig.Options.PING_MODE)
        {
            return name + this.getTranslation(PING_MODE, this.pingMode);
        }
        else if (options == ExtendedConfig.Options.VISIT_ISLAND_MODE)
        {
            return name + this.getTranslation(VISIT_ISLAND_MODE, this.visitIslandMode);
        }
        else if (options == ExtendedConfig.Options.RARE_DROP_MODE)
        {
            return name + this.getTranslation(RARE_DROP_MODE, this.itemDropMode);
        }
        else
        {
            return name;
        }
    }

    public void setOptionValue(ExtendedConfig.Options options, int value)
    {
        if (options == ExtendedConfig.Options.PREVIEW)
        {
            GuiExtendedConfig.preview = !GuiExtendedConfig.preview;
        }
        else if (options == ExtendedConfig.Options.SWAP_INFO_POS)
        {
            this.swapRenderInfo = !this.swapRenderInfo;
        }
        else if (options == ExtendedConfig.Options.HEALTH_STATUS)
        {
            this.healthStatusMode = (this.healthStatusMode + value) % 3;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_ORDERING)
        {
            this.equipmentOrdering = (this.equipmentOrdering + value) % 2;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_DIRECTION)
        {
            this.equipmentDirection = (this.equipmentDirection + value) % 2;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_STATUS)
        {
            this.equipmentStatus = (this.equipmentStatus + value) % 6;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_POSITION)
        {
            this.equipmentPosition = (this.equipmentPosition + value) % 3;
        }
        else if (options == ExtendedConfig.Options.POTION_HUD_STYLE)
        {
            this.potionHUDStyle = (this.potionHUDStyle + value) % 2;
        }
        else if (options == ExtendedConfig.Options.POTION_HUD_POSITION)
        {
            this.potionHUDPosition = (this.potionHUDPosition + value) % 4;
        }
        else if (options == ExtendedConfig.Options.PING_MODE)
        {
            this.pingMode = (this.pingMode + value) % 2;
        }
        else if (options == ExtendedConfig.Options.VISIT_ISLAND_MODE)
        {
            this.visitIslandMode = (this.visitIslandMode + value) % 3;
        }
        else if (options == ExtendedConfig.Options.RARE_DROP_MODE)
        {
            this.itemDropMode = (this.itemDropMode + value) % 2;
        }

        else if (options == ExtendedConfig.Options.FPS)
        {
            this.fps = !this.fps;
        }
        else if (options == ExtendedConfig.Options.XYZ)
        {
            this.xyz = !this.xyz;
        }
        else if (options == ExtendedConfig.Options.DIRECTION)
        {
            this.direction = !this.direction;
        }
        else if (options == ExtendedConfig.Options.BIOME)
        {
            this.biome = !this.biome;
        }
        else if (options == ExtendedConfig.Options.PING)
        {
            this.ping = !this.ping;
        }
        else if (options == ExtendedConfig.Options.PING_TO_SECOND)
        {
            this.pingToSecond = !this.pingToSecond;
        }
        else if (options == ExtendedConfig.Options.SERVER_IP)
        {
            this.serverIP = !this.serverIP;
        }
        else if (options == ExtendedConfig.Options.SERVER_IP_MC)
        {
            this.serverIPMCVersion = !this.serverIPMCVersion;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_HUD)
        {
            this.equipmentHUD = !this.equipmentHUD;
        }
        else if (options == ExtendedConfig.Options.POTION_HUD)
        {
            this.potionHUD = !this.potionHUD;
        }
        else if (options == ExtendedConfig.Options.REAL_TIME)
        {
            this.realTime = !this.realTime;
        }
        else if (options == ExtendedConfig.Options.GAME_TIME)
        {
            this.gameTime = !this.gameTime;
        }
        else if (options == ExtendedConfig.Options.GAME_WEATHER)
        {
            this.gameWeather = !this.gameWeather;
        }
        else if (options == ExtendedConfig.Options.MOON_PHASE)
        {
            this.moonPhase = !this.moonPhase;
        }
        else if (options == ExtendedConfig.Options.POTION_ICON)
        {
            this.potionHUDIcon = !this.potionHUDIcon;
        }
        else if (options == ExtendedConfig.Options.ALTERNATE_POTION_COLOR)
        {
            this.alternatePotionHUDTextColor = !this.alternatePotionHUDTextColor;
        }

        else if (options == ExtendedConfig.Options.RIGHT_CLICK_ADD_PARTY)
        {
            this.rightClickToAddParty = !this.rightClickToAddParty;
        }
        else if (options == ExtendedConfig.Options.ADD_PARTY_VISIT_ISLAND)
        {
            this.addPartyVisitIsland = !this.addPartyVisitIsland;
        }
        else if (options == ExtendedConfig.Options.JUNGLE_AXE_OVERLAY)
        {
            this.jungleAxeOverlay = !this.jungleAxeOverlay;
        }
        else if (options == ExtendedConfig.Options.GRAPPLING_HOOK_OVERLAY)
        {
            this.grapplingHookOverlay = !this.grapplingHookOverlay;
        }
        else if (options == ExtendedConfig.Options.ZEALOT_RESPAWN_OVERLAY)
        {
            this.zealotRespawnOverlay = !this.zealotRespawnOverlay;
        }
        else if (options == ExtendedConfig.Options.DRAGON_SET_GLOWING_EYE)
        {
            this.dragonSetGlowingEye = !this.dragonSetGlowingEye;
        }
    }

    public void setOptionFloatValue(ExtendedConfig.Options options, float value)
    {
        if (options == ExtendedConfig.Options.ARMOR_HUD_Y)
        {
            this.armorHUDYOffset = (int) value;
        }
        else if (options == ExtendedConfig.Options.POTION_HUD_Y)
        {
            this.potionHUDYOffset = (int) value;
        }
        else if (options == ExtendedConfig.Options.MAXIMUM_POTION_DISPLAY)
        {
            this.maximumPotionDisplay = (int) value;
        }
        else if (options == ExtendedConfig.Options.POTION_LENGTH_Y_OFFSET)
        {
            this.potionLengthYOffset = (int) value;
        }
        else if (options == ExtendedConfig.Options.POTION_LENGTH_Y_OFFSET_OVERLAP)
        {
            this.potionLengthYOffsetOverlap = (int) value;
        }

        else if (options == ExtendedConfig.Options.JUNGLE_AXE_DELAY)
        {
            this.jungleAxeDelay = (int) value;
        }
        else if (options == ExtendedConfig.Options.GRAPPLING_HOOK_DELAY)
        {
            this.grapplingHookDelay = (int) value;
        }
        else if (options == ExtendedConfig.Options.ZEALOT_RESPAWN_DELAY)
        {
            this.zealotRespawnDelay = (int) value;
        }
    }

    public void setOptionStringValue(ExtendedConfig.Options options, String value)
    {
        if (options == ExtendedConfig.Options.FPS_COLOR)
        {
            this.fpsColor = value;
        }
        else if (options == ExtendedConfig.Options.XYZ_COLOR)
        {
            this.xyzColor = value;
        }
        else if (options == ExtendedConfig.Options.BIOME_COLOR)
        {
            this.biomeColor = value;
        }
        else if (options == ExtendedConfig.Options.DIRECTION_COLOR)
        {
            this.directionColor = value;
        }
        else if (options == ExtendedConfig.Options.PING_COLOR)
        {
            this.pingColor = value;
        }
        else if (options == ExtendedConfig.Options.PING_TO_SECOND_COLOR)
        {
            this.pingToSecondColor = value;
        }
        else if (options == ExtendedConfig.Options.SERVER_IP_COLOR)
        {
            this.serverIPColor = value;
        }
        else if (options == ExtendedConfig.Options.EQUIPMENT_STATUS_COLOR)
        {
            this.equipmentStatusColor = value;
        }
        else if (options == ExtendedConfig.Options.ARROW_COUNT_COLOR)
        {
            this.arrowCountColor = value;
        }
        else if (options == ExtendedConfig.Options.REAL_TIME_COLOR)
        {
            this.realTimeColor = value;
        }
        else if (options == ExtendedConfig.Options.GAME_TIME_COLOR)
        {
            this.gameTimeColor = value;
        }
        else if (options == ExtendedConfig.Options.GAME_WEATHER_COLOR)
        {
            this.gameWeatherColor = value;
        }
        else if (options == ExtendedConfig.Options.MOON_PHASE_COLOR)
        {
            this.moonPhaseColor = value;
        }
        else if (options == ExtendedConfig.Options.JUNGLE_AXE_DELAY_COLOR)
        {
            this.jungleAxeDelayColor = value;
        }
        else if (options == ExtendedConfig.Options.GRAPPLING_HOOK_DELAY_COLOR)
        {
            this.grapplingHookDelayColor = value;
        }
        else if (options == ExtendedConfig.Options.ZEALOT_RESPAWN_DELAY_COLOR)
        {
            this.zealotRespawnDelayColor = value;
        }

        else if (options == ExtendedConfig.Options.FPS_VALUE_COLOR)
        {
            this.fpsValueColor = value;
        }
        else if (options == ExtendedConfig.Options.FPS_26_AND_40_COLOR)
        {
            this.fps26And49Color = value;
        }
        else if (options == ExtendedConfig.Options.FPS_LOW_25_COLOR)
        {
            this.fpsLow25Color = value;
        }
        else if (options == ExtendedConfig.Options.XYZ_VALUE_COLOR)
        {
            this.xyzValueColor = value;
        }
        else if (options == ExtendedConfig.Options.DIRECTION_VALUE_COLOR)
        {
            this.directionValueColor = value;
        }
        else if (options == ExtendedConfig.Options.BIOME_VALUE_COLOR)
        {
            this.biomeValueColor = value;
        }
        else if (options == ExtendedConfig.Options.PING_VALUE_COLOR)
        {
            this.pingValueColor = value;
        }
        else if (options == ExtendedConfig.Options.PING_200_AND_300_COLOR)
        {
            this.ping200And300Color = value;
        }
        else if (options == ExtendedConfig.Options.PING_300_AND_500_COLOR)
        {
            this.ping300And500Color = value;
        }
        else if (options == ExtendedConfig.Options.PING_MAX_500_COLOR)
        {
            this.pingMax500Color = value;
        }
        else if (options == ExtendedConfig.Options.SERVER_IP_VALUE_COLOR)
        {
            this.serverIPValueColor = value;
        }
        else if (options == ExtendedConfig.Options.REAL_TIME_HHMMSS_VALUE_COLOR)
        {
            this.realTimeHHMMSSValueColor = value;
        }
        else if (options == ExtendedConfig.Options.REAL_TIME_DDMMYY_VALUE_COLOR)
        {
            this.realTimeDDMMYYValueColor = value;
        }
        else if (options == ExtendedConfig.Options.GAME_TIME_VALUE_COLOR)
        {
            this.gameTimeValueColor = value;
        }
        else if (options == ExtendedConfig.Options.GAME_WEATHER_VALUE_COLOR)
        {
            this.gameWeatherValueColor = value;
        }
        else if (options == ExtendedConfig.Options.MOON_PHASE_VALUE_COLOR)
        {
            this.moonPhaseValueColor = value;
        }
    }

    public float getOptionFloatValue(ExtendedConfig.Options settingOption)
    {
        if (settingOption == ExtendedConfig.Options.ARMOR_HUD_Y)
        {
            return this.armorHUDYOffset;
        }
        else if (settingOption == ExtendedConfig.Options.POTION_HUD_Y)
        {
            return this.potionHUDYOffset;
        }
        else if (settingOption == ExtendedConfig.Options.MAXIMUM_POTION_DISPLAY)
        {
            return this.maximumPotionDisplay;
        }
        else if (settingOption == ExtendedConfig.Options.POTION_LENGTH_Y_OFFSET)
        {
            return this.potionLengthYOffset;
        }
        else if (settingOption == ExtendedConfig.Options.POTION_LENGTH_Y_OFFSET_OVERLAP)
        {
            return this.potionLengthYOffsetOverlap;
        }

        else if (settingOption == ExtendedConfig.Options.JUNGLE_AXE_DELAY)
        {
            return this.jungleAxeDelay;
        }
        else if (settingOption == ExtendedConfig.Options.GRAPPLING_HOOK_DELAY)
        {
            return this.grapplingHookDelay;
        }
        else if (settingOption == ExtendedConfig.Options.ZEALOT_RESPAWN_DELAY)
        {
            return this.zealotRespawnDelay;
        }
        return 0.0F;
    }

    public boolean getOptionOrdinalValue(ExtendedConfig.Options options)
    {
        switch (options)
        {
        case PREVIEW:
            return GuiExtendedConfig.preview;
        case SWAP_INFO_POS:
            return this.swapRenderInfo;
        case FPS:
            return this.fps;
        case XYZ:
            return this.xyz;
        case DIRECTION:
            return this.direction;
        case BIOME:
            return this.biome;
        case PING:
            return this.ping;
        case PING_TO_SECOND:
            return this.pingToSecond;
        case SERVER_IP:
            return this.serverIP;
        case SERVER_IP_MC:
            return this.serverIPMCVersion;
        case EQUIPMENT_HUD:
            return this.equipmentHUD;
        case POTION_HUD:
            return this.potionHUD;
        case REAL_TIME:
            return this.realTime;
        case GAME_TIME:
            return this.gameTime;
        case GAME_WEATHER:
            return this.gameWeather;
        case MOON_PHASE:
            return this.moonPhase;
        case POTION_ICON:
            return this.potionHUDIcon;
        case ALTERNATE_POTION_COLOR:
            return this.alternatePotionHUDTextColor;

        case RIGHT_CLICK_ADD_PARTY:
            return this.rightClickToAddParty;
        case ADD_PARTY_VISIT_ISLAND:
            return this.addPartyVisitIsland;
        case JUNGLE_AXE_OVERLAY:
            return this.jungleAxeOverlay;
        case GRAPPLING_HOOK_OVERLAY:
            return this.grapplingHookOverlay;
        case ZEALOT_RESPAWN_OVERLAY:
            return this.zealotRespawnOverlay;
        case DRAGON_SET_GLOWING_EYE:
            return this.dragonSetGlowingEye;
        default:
            return false;
        }
    }

    public String getOptionStringValue(ExtendedConfig.Options options)
    {
        switch (options)
        {
        case FPS_COLOR:
            return this.fpsColor;
        case XYZ_COLOR:
            return this.xyzColor;
        case BIOME_COLOR:
            return this.biomeColor;
        case DIRECTION_COLOR:
            return this.directionColor;
        case PING_COLOR:
            return this.pingColor;
        case PING_TO_SECOND_COLOR:
            return this.pingToSecondColor;
        case SERVER_IP_COLOR:
            return this.serverIPColor;
        case EQUIPMENT_STATUS_COLOR:
            return this.equipmentStatusColor;
        case ARROW_COUNT_COLOR:
            return this.arrowCountColor;
        case REAL_TIME_COLOR:
            return this.realTimeColor;
        case GAME_TIME_COLOR:
            return this.gameTimeColor;
        case GAME_WEATHER_COLOR:
            return this.gameWeatherColor;
        case MOON_PHASE_COLOR:
            return this.moonPhaseColor;
        case JUNGLE_AXE_DELAY_COLOR:
            return this.jungleAxeDelayColor;
        case GRAPPLING_HOOK_DELAY_COLOR:
            return this.grapplingHookDelayColor;
        case ZEALOT_RESPAWN_DELAY_COLOR:
            return this.zealotRespawnDelayColor;

        case FPS_VALUE_COLOR:
            return this.fpsValueColor;
        case FPS_26_AND_40_COLOR:
            return this.fps26And49Color;
        case FPS_LOW_25_COLOR:
            return this.fpsLow25Color;
        case XYZ_VALUE_COLOR:
            return this.xyzValueColor;
        case DIRECTION_VALUE_COLOR:
            return this.directionValueColor;
        case BIOME_VALUE_COLOR:
            return this.biomeValueColor;
        case PING_VALUE_COLOR:
            return this.pingValueColor;
        case PING_200_AND_300_COLOR:
            return this.ping200And300Color;
        case PING_300_AND_500_COLOR:
            return this.ping300And500Color;
        case PING_MAX_500_COLOR:
            return this.pingMax500Color;
        case SERVER_IP_VALUE_COLOR:
            return this.serverIPValueColor;
        case REAL_TIME_HHMMSS_VALUE_COLOR:
            return this.realTimeHHMMSSValueColor;
        case REAL_TIME_DDMMYY_VALUE_COLOR:
            return this.realTimeDDMMYYValueColor;
        case GAME_TIME_VALUE_COLOR:
            return this.gameTimeValueColor;
        case GAME_WEATHER_VALUE_COLOR:
            return this.gameWeatherValueColor;
        case MOON_PHASE_VALUE_COLOR:
            return this.moonPhaseValueColor;
        default:
            return "";
        }
    }

    private String getTranslation(String[] strArray, int index)
    {
        if (index < 0 || index >= strArray.length)
        {
            index = 0;
        }
        return LangUtils.translate(strArray[index]);
    }

    @SideOnly(Side.CLIENT)
    public static enum Options
    {
        PREVIEW(false, true),

        SWAP_INFO_POS(false, true),
        HEALTH_STATUS(false, false),
        EQUIPMENT_ORDERING(false, false),
        EQUIPMENT_DIRECTION(false, false),
        EQUIPMENT_STATUS(false, false),
        EQUIPMENT_POSITION(false, false),
        POTION_HUD_STYLE(false, false),
        POTION_HUD_POSITION(false, false),
        PING_MODE(false, false),
        VISIT_ISLAND_MODE(false, false),
        RARE_DROP_MODE(false, false),

        FPS(false, true),
        XYZ(false, true),
        DIRECTION(false, true),
        BIOME(false, true),
        PING(false, true),
        PING_TO_SECOND(false, true),
        SERVER_IP(false, true),
        SERVER_IP_MC(false, true),
        EQUIPMENT_HUD(false, true),
        POTION_HUD(false, true),
        REAL_TIME(false, true),
        GAME_TIME(false, true),
        GAME_WEATHER(false, true),
        MOON_PHASE(false, true),
        POTION_ICON(false, true),
        ALTERNATE_POTION_COLOR(false, true),

        ARMOR_HUD_Y(true, false, -512.0F, 512.0F, 1.0F),
        POTION_HUD_Y(true, false, -512.0F, 512.0F, 1.0F),
        MAXIMUM_POTION_DISPLAY(true, false, 2.0F, 8.0F, 1.0F),
        POTION_LENGTH_Y_OFFSET(true, false, 1.0F, 256.0F, 1.0F),
        POTION_LENGTH_Y_OFFSET_OVERLAP(true, false, 1.0F, 256.0F, 1.0F),

        FPS_COLOR(false, false, true),
        XYZ_COLOR(false, false, true),
        BIOME_COLOR(false, false, true),
        DIRECTION_COLOR(false, false, true),
        PING_COLOR(false, false, true),
        PING_TO_SECOND_COLOR(false, false, true),
        SERVER_IP_COLOR(false, false, true),
        EQUIPMENT_STATUS_COLOR(false, false, true),
        ARROW_COUNT_COLOR(false, false, true),
        REAL_TIME_COLOR(false, false, true),
        GAME_TIME_COLOR(false, false, true),
        GAME_WEATHER_COLOR(false, false, true),
        MOON_PHASE_COLOR(false, false, true),
        JUNGLE_AXE_DELAY_COLOR(false, false, true),
        GRAPPLING_HOOK_DELAY_COLOR(false, false, true),
        ZEALOT_RESPAWN_DELAY_COLOR(false, false, true),

        FPS_VALUE_COLOR(false, false, true),
        FPS_26_AND_40_COLOR(false, false, true),
        FPS_LOW_25_COLOR(false, false, true),
        XYZ_VALUE_COLOR(false, false, true),
        DIRECTION_VALUE_COLOR(false, false, true),
        BIOME_VALUE_COLOR(false, false, true),
        PING_VALUE_COLOR(false, false, true),
        PING_200_AND_300_COLOR(false, false, true),
        PING_300_AND_500_COLOR(false, false, true),
        PING_MAX_500_COLOR(false, false, true),
        SERVER_IP_VALUE_COLOR(false, false, true),
        REAL_TIME_HHMMSS_VALUE_COLOR(false, false, true),
        REAL_TIME_DDMMYY_VALUE_COLOR(false, false, true),
        GAME_TIME_VALUE_COLOR(false, false, true),
        GAME_WEATHER_VALUE_COLOR(false, false, true),
        MOON_PHASE_VALUE_COLOR(false, false, true),

        RIGHT_CLICK_ADD_PARTY(false, true),
        ADD_PARTY_VISIT_ISLAND(false, true),
        JUNGLE_AXE_OVERLAY(false, true),
        GRAPPLING_HOOK_OVERLAY(false, true),
        ZEALOT_RESPAWN_OVERLAY(false, true),
        DRAGON_SET_GLOWING_EYE(false, true),
        JUNGLE_AXE_DELAY(true, false, 1500.0F, 2000.0F, 50.0F),
        GRAPPLING_HOOK_DELAY(true, false, 1500.0F, 2000.0F, 50.0F),
        ZEALOT_RESPAWN_DELAY(true, false, 10000.0F, 15000.0F, 50.0F),
        ;

        private final boolean isFloat;
        private final boolean isBoolean;
        private final float valueStep;
        private boolean isTextbox;
        private float valueMin;
        private float valueMax;
        private static final Options[] values = Options.values();

        public static Options byOrdinal(int ordinal)
        {
            for (Options options : values)
            {
                if (options.getOrdinal() == ordinal)
                {
                    return options;
                }
            }
            return null;
        }

        private Options(boolean isFloat, boolean isBoolean)
        {
            this(isFloat, isBoolean, false, 0.0F, 1.0F, 0.0F);
        }

        private Options(boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep)
        {
            this(isFloat, isBoolean, false, valMin, valMax, valStep);
        }

        private Options(boolean isFloat, boolean isBoolean, boolean isTextbox)
        {
            this(isFloat, isBoolean, isTextbox, 0.0F, 1.0F, 0.0F);
        }

        private Options(boolean isFloat, boolean isBoolean, boolean isTextbox, float valMin, float valMax, float valStep)
        {
            this.isFloat = isFloat;
            this.isBoolean = isBoolean;
            this.isTextbox = isTextbox;
            this.valueMin = valMin;
            this.valueMax = valMax;
            this.valueStep = valStep;
        }

        public boolean isFloat()
        {
            return this.isFloat;
        }

        public boolean isBoolean()
        {
            return this.isBoolean;
        }

        public boolean isTextbox()
        {
            return this.isTextbox;
        }

        public int getOrdinal()
        {
            return this.ordinal();
        }

        public String getTranslation()
        {
            return LangUtils.translate(this.name().toLowerCase() + ".extended_config");
        }

        public float getValueMin()
        {
            return this.valueMin;
        }

        public float getValueMax()
        {
            return this.valueMax;
        }

        public void setValueMax(float value)
        {
            this.valueMax = value;
        }

        public float normalizeValue(float value)
        {
            return MathHelper.clamp_float((this.snapToStepClamp(value) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value)
        {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value)
        {
            value = this.snapToStep(value);
            return MathHelper.clamp_float(value, this.valueMin, this.valueMax);
        }

        private float snapToStep(float value)
        {
            if (this.valueStep > 0.0F)
            {
                value = this.valueStep * Math.round(value / this.valueStep);
            }
            return value;
        }
    }
}