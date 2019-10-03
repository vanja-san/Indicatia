package stevekung.mods.indicatia.renderer;

import java.text.DateFormat;
import java.util.*;

import com.google.common.math.DoubleMath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeVersion;
import stevekung.mods.indicatia.config.EnumEquipment;
import stevekung.mods.indicatia.config.EnumPotionStatus;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.event.HypixelEventHandler;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.InfoUtils;
import stevekung.mods.indicatia.utils.LangUtils;

public class HUDInfo
{
    private static final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");

    public static String getFPS()
    {
        int fps = Minecraft.getDebugFPS();
        String color = ColorUtils.stringToRGB(ExtendedConfig.instance.fpsValueColor).toColoredFont();

        if (fps >= 26 && fps <= 49)
        {
            color = ColorUtils.stringToRGB(ExtendedConfig.instance.fps26And49Color).toColoredFont();
        }
        else if (fps <= 25)
        {
            color = ColorUtils.stringToRGB(ExtendedConfig.instance.fpsLow25Color).toColoredFont();
        }
        return ColorUtils.stringToRGB(ExtendedConfig.instance.fpsColor).toColoredFont() + "FPS: " + color + fps;
    }

    public static String getXYZ(Minecraft mc)
    {
        BlockPos pos = new BlockPos(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        String nether = mc.thePlayer.dimension == -1 ? "Nether " : "";
        return ColorUtils.stringToRGB(ExtendedConfig.instance.xyzColor).toColoredFont() + nether + "XYZ: " + ColorUtils.stringToRGB(ExtendedConfig.instance.xyzValueColor).toColoredFont() + x + " " + y + " " + z;
    }

    public static String getOverworldXYZFromNether(Minecraft mc)
    {
        BlockPos pos = new BlockPos(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return ColorUtils.stringToRGB(ExtendedConfig.instance.xyzColor).toColoredFont() + "Overworld XYZ: " + ColorUtils.stringToRGB(ExtendedConfig.instance.xyzValueColor).toColoredFont() + x * 8 + " " + y + " " + z * 8;
    }

    public static String getBiome(Minecraft mc)
    {
        BlockPos pos = new BlockPos(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().getEntityBoundingBox().minY, mc.getRenderViewEntity().posZ);
        Chunk chunk = mc.theWorld.getChunkFromBlockCoords(pos);

        if (mc.theWorld.isBlockLoaded(pos) && pos.getY() >= 0 && pos.getY() < 256)
        {
            if (!chunk.isEmpty())
            {
                String biomeName = chunk.getBiome(pos, mc.theWorld.getWorldChunkManager()).biomeName.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
                return ColorUtils.stringToRGB(ExtendedConfig.instance.biomeColor).toColoredFont() + "Biome: " + ColorUtils.stringToRGB(ExtendedConfig.instance.biomeValueColor).toColoredFont() + biomeName;
            }
            else
            {
                return "Waiting for chunk...";
            }
        }
        else
        {
            return "Outside of world...";
        }
    }

    public static String getPing()
    {
        int responseTime = InfoUtils.INSTANCE.getPing();
        return ColorUtils.stringToRGB(ExtendedConfig.instance.pingColor).toColoredFont() + "Ping: " + HUDInfo.getResponseTimeColor(responseTime) + responseTime + "ms";
    }

    public static String getPingToSecond()
    {
        double responseTime = InfoUtils.INSTANCE.getPing() / 1000D;
        return ColorUtils.stringToRGB(ExtendedConfig.instance.pingToSecondColor).toColoredFont() + "Delay: " + HUDInfo.getResponseTimeColor((int) (responseTime * 1000D)) + responseTime + "s";
    }

    public static String getServerIP(Minecraft mc)
    {
        String ip = ColorUtils.stringToRGB(ExtendedConfig.instance.serverIPColor).toColoredFont() + "IP: " + "" + ColorUtils.stringToRGB(ExtendedConfig.instance.serverIPValueColor).toColoredFont() + mc.getCurrentServerData().serverIP;

        if (ExtendedConfig.instance.serverIPMCVersion)
        {
            ip = ip + "/" + ForgeVersion.mcVersion;
        }
        return ip;
    }

    public static String renderDirection(Minecraft mc)
    {
        Entity entity = mc.getRenderViewEntity();
        int yaw = (int)entity.rotationYaw + 22;
        String direction;

        yaw %= 360;

        if (yaw < 0)
        {
            yaw += 360;
        }

        int facing = yaw / 45;

        if (facing < 0)
        {
            facing = 7;
        }

        EnumFacing coordFacing = entity.getHorizontalFacing();
        String coord = "";

        switch (coordFacing)
        {
        default:
        case NORTH:
            coord = "-Z";
            break;
        case SOUTH:
            coord = "+Z";
            break;
        case WEST:
            coord = "-X";
            break;
        case EAST:
            coord = "+X";
            break;
        }

        switch (facing)
        {
        case 0:
            direction = "South";
            break;
        case 1:
            direction = "South West";
            break;
        case 2:
            direction = "West";
            break;
        case 3:
            direction = "North West";
            break;
        case 4:
            direction = "North";
            break;
        case 5:
            direction = "North East";
            break;
        case 6:
            direction = "East";
            break;
        case 7:
            direction = "South East";
            break;
        default:
            direction = "Unknown";
            break;
        }
        direction += " (" + coord + ")";
        return ColorUtils.stringToRGB(ExtendedConfig.instance.directionColor).toColoredFont() + "Direction: " + ColorUtils.stringToRGB(ExtendedConfig.instance.directionValueColor).toColoredFont() + direction;
    }

    public static String getCurrentTime()
    {
        Date date = new Date();
        boolean isThai = Calendar.getInstance().getTimeZone().getID().equals("Asia/Bangkok");
        String dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, isThai ? new Locale("th", "TH") : Locale.getDefault()).format(date);
        String timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, isThai ? new Locale("th", "TH") : Locale.getDefault()).format(date);
        String currentTime = ColorUtils.stringToRGB(ExtendedConfig.instance.realTimeDDMMYYValueColor).toColoredFont() + dateFormat + " " + ColorUtils.stringToRGB(ExtendedConfig.instance.realTimeHHMMSSValueColor).toColoredFont() + timeFormat;
        return ColorUtils.stringToRGB(ExtendedConfig.instance.realTimeColor).toColoredFont() + "Time: " + currentTime;
    }

    public static String getCurrentGameTime(Minecraft mc)
    {
        if (HypixelEventHandler.isSkyBlock && IndicatiaMod.isSkyblockAddonsLoaded)
        {
            StringBuilder builder = new StringBuilder();

            try
            {
                Class<?> skyblockAddons = Class.forName("codes.biscuit.skyblockaddons.SkyblockAddons");
                Object getInstance = skyblockAddons.getDeclaredMethod("getInstance").invoke(skyblockAddons);
                Object getUtils = getInstance.getClass().getDeclaredMethod("getUtils").invoke(getInstance);
                Object getCurrentDate = getUtils.getClass().getDeclaredMethod("getCurrentDate").invoke(getUtils);
                Class<?> date = getCurrentDate.getClass(); // SkyblockDate
                builder.append(date.getDeclaredMethod("getHour").invoke(getCurrentDate));
                builder.append(":");
                int minute = (int)date.getDeclaredMethod("getMinute").invoke(getCurrentDate);
                builder.append(minute == 0 ? "0" + minute : minute);
                builder.append(HypixelEventHandler.SKYBLOCK_AMPM);
            }
            catch (Exception e) {}
            String currentTime = ColorUtils.stringToRGB(ExtendedConfig.instance.realTimeDDMMYYValueColor).toColoredFont() + builder.toString();
            return ColorUtils.stringToRGB(ExtendedConfig.instance.realTimeColor).toColoredFont() + "Skyblock Time: " + currentTime;
        }
        return InfoUtils.INSTANCE.getCurrentGameTime(mc.theWorld.getWorldTime() % 24000);
    }

    public static String getGameWeather(Minecraft mc)
    {
        String weather = mc.theWorld.isRaining() && !mc.theWorld.isThundering() ? "Raining" : mc.theWorld.isRaining() && mc.theWorld.isThundering() ? "Thunder" : "";
        return ColorUtils.stringToRGB(ExtendedConfig.instance.gameWeatherColor).toColoredFont() + "Weather: " + ColorUtils.stringToRGB(ExtendedConfig.instance.gameWeatherValueColor).toColoredFont() + weather;
    }

    public static void renderHorizontalEquippedItems(Minecraft mc)
    {
        String ordering = EnumEquipment.Ordering.getById(ExtendedConfig.instance.equipmentOrdering);
        ScaledResolution res = new ScaledResolution(mc);
        boolean isRightSide = EnumEquipment.Position.getById(ExtendedConfig.instance.equipmentPosition).equalsIgnoreCase("right");
        int baseXOffset = 2;
        int baseYOffset = ExtendedConfig.instance.armorHUDYOffset;
        ItemStack mainHandItem = mc.thePlayer.getCurrentEquippedItem();
        List<HorizontalEquipment> element = new ArrayList<>();
        int prevX = 0;
        int rightWidth = 0;
        element.clear();

        // held item stuff
        if (ordering.equals("reverse"))
        {
            if (mainHandItem != null)
            {
                element.add(new HorizontalEquipment(mainHandItem, false));
            }
        }

        // armor stuff
        switch (ordering)
        {
        case "default":
            for (int i = 3; i >= 0; i--)
            {
                if (mc.thePlayer.inventory.armorInventory[i] != null)
                {
                    element.add(new HorizontalEquipment(mc.thePlayer.inventory.armorInventory[i], mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable()));
                }
            }
            break;
        case "reverse":
            for (int i = 0; i <= 3; i++)
            {
                if (mc.thePlayer.inventory.armorInventory[i] != null)
                {
                    element.add(new HorizontalEquipment(mc.thePlayer.inventory.armorInventory[i], mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable()));
                }
            }
            break;
        }

        // held item stuff
        if (ordering.equals("default"))
        {
            if (mainHandItem != null)
            {
                element.add(new HorizontalEquipment(mainHandItem, false));
            }
        }

        for (HorizontalEquipment equipment : element)
        {
            rightWidth += equipment.getWidth();
        }
        for (HorizontalEquipment equipment : element)
        {
            int xBaseRight = res.getScaledWidth() - rightWidth - baseXOffset;
            equipment.render(isRightSide ? xBaseRight + prevX + equipment.getWidth() : baseXOffset + prevX, baseYOffset);
            prevX += equipment.getWidth();
        }
    }

    public static void renderVerticalEquippedItems(Minecraft mc)
    {
        String ordering = EnumEquipment.Ordering.getById(ExtendedConfig.instance.equipmentOrdering);
        String status = EnumEquipment.Status.getById(ExtendedConfig.instance.equipmentStatus);
        List<ItemStack> itemStackList = new ArrayList<>();
        List<String> itemStatusList = new ArrayList<>();
        List<String> arrowCountList = new ArrayList<>();
        ScaledResolution res = new ScaledResolution(mc);
        boolean isRightSide = EnumEquipment.Position.getById(ExtendedConfig.instance.equipmentPosition).equalsIgnoreCase("right");
        int baseXOffset = isRightSide ? res.getScaledWidth() - 18 : 2;
        int baseYOffset = ExtendedConfig.instance.armorHUDYOffset;
        ItemStack mainHandItem = mc.thePlayer.getHeldItem();
        int arrowCount = HUDInfo.getInventoryArrowCount(mc.thePlayer.inventory);

        // held item stuff
        if (ordering.equals("reverse"))
        {
            if (mainHandItem != null)
            {
                itemStackList.add(mainHandItem);
                String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mainHandItem);
                itemStatusList.add(mainHandItem.isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mainHandItem) : status.equals("none") ? "" : HUDInfo.getItemStackCount(mainHandItem, Integer.parseInt(itemCount)));

                if (mainHandItem.getItem() == Items.bow)
                {
                    arrowCountList.add(HUDInfo.getArrowStackCount(arrowCount));
                }
                else
                {
                    arrowCountList.add(""); // dummy bow arrow count list size
                }
            }
        }

        // armor stuff
        switch (ordering)
        {
        case "default":
            for (int i = 3; i >= 0; i--)
            {
                if (mc.thePlayer.inventory.armorInventory[i] != null)
                {
                    String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mc.thePlayer.inventory.armorInventory[i]);
                    itemStackList.add(mc.thePlayer.inventory.armorInventory[i]);
                    itemStatusList.add(mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mc.thePlayer.inventory.armorInventory[i]) : HUDInfo.getItemStackCount(mc.thePlayer.inventory.armorInventory[i], Integer.parseInt(itemCount)));
                    arrowCountList.add(""); // dummy bow arrow count list size
                }
            }
            break;
        case "reverse":
            for (int i = 0; i <= 3; i++)
            {
                if (mc.thePlayer.inventory.armorInventory[i] != null)
                {
                    String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mc.thePlayer.inventory.armorInventory[i]);
                    itemStackList.add(mc.thePlayer.inventory.armorInventory[i]);
                    itemStatusList.add(mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mc.thePlayer.inventory.armorInventory[i]) : HUDInfo.getItemStackCount(mc.thePlayer.inventory.armorInventory[i], Integer.parseInt(itemCount)));
                    arrowCountList.add(""); // dummy bow arrow count list size
                }
            }
            break;
        }

        // held item stuff
        if (ordering.equals("default"))
        {
            if (mainHandItem != null)
            {
                itemStackList.add(mainHandItem);
                String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mainHandItem);
                itemStatusList.add(mainHandItem.isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mainHandItem) : status.equals("none") ? "" : HUDInfo.getItemStackCount(mainHandItem, Integer.parseInt(itemCount)));

                if (mainHandItem.getItem() == Items.bow)
                {
                    arrowCountList.add(HUDInfo.getArrowStackCount(arrowCount));
                }
                else
                {
                    arrowCountList.add(""); // dummy bow arrow count list size
                }
            }
        }

        // item render stuff
        for (int i = 0; i < itemStackList.size(); ++i)
        {
            ItemStack itemStack = itemStackList.get(i);

            if (!itemStackList.isEmpty())
            {
                int yOffset = baseYOffset + 16 * i;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
                yOffset += 16;
            }
        }

        float yOffset = 0;
        float fontHeight = 0;

        // durability/item count stuff
        for (int i = 0; i < itemStatusList.size(); ++i)
        {
            String string = itemStatusList.get(i);
            fontHeight = mc.fontRendererObj.FONT_HEIGHT + 7.0625F;

            if (!string.isEmpty())
            {
                yOffset = baseYOffset + 4 + fontHeight * i;
                float xOffset = isRightSide ? res.getScaledWidth() - mc.fontRendererObj.getStringWidth(string) - 20.0625F : baseXOffset + 18.0625F;
                mc.fontRendererObj.drawString(ColorUtils.stringToRGB(ExtendedConfig.instance.equipmentStatusColor).toColoredFont() + string, xOffset, yOffset, 16777215, true);
            }
        }

        // arrow count stuff
        for (int i = 0; i < arrowCountList.size(); ++i)
        {
            String string = arrowCountList.get(i);
            yOffset = baseYOffset + 8 + fontHeight * i;

            if (!string.isEmpty())
            {
                GlStateManager.disableDepth();
                ColorUtils.unicodeFontRenderer.drawString(ColorUtils.stringToRGB(ExtendedConfig.instance.arrowCountColor).toColoredFont() + string, isRightSide ? res.getScaledWidth() - ColorUtils.unicodeFontRenderer.getStringWidth(string) - 2.0625F : baseXOffset + 8.0625F, yOffset, 16777215, true);
                GlStateManager.enableDepth();
            }
        }
    }

    public static void renderHotbarEquippedItems(Minecraft mc)
    {
        List<ItemStack> leftItemStackList = new ArrayList<>();
        List<String> leftItemStatusList = new ArrayList<>();
        List<String> leftArrowCountList = new ArrayList<>();
        List<ItemStack> rightItemStackList = new ArrayList<>();
        List<String> rightItemStatusList = new ArrayList<>();
        ScaledResolution res = new ScaledResolution(mc);
        ItemStack mainHandItem = mc.thePlayer.getHeldItem();
        int arrowCount = HUDInfo.getInventoryArrowCount(mc.thePlayer.inventory);
        String status = EnumEquipment.Status.getById(ExtendedConfig.instance.equipmentStatus);

        for (int i = 2; i <= 3; i++)
        {
            if (mc.thePlayer.inventory.armorInventory[i] != null)
            {
                String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mc.thePlayer.inventory.armorInventory[i]);
                leftItemStackList.add(mc.thePlayer.inventory.armorInventory[i]);
                leftItemStatusList.add(mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mc.thePlayer.inventory.armorInventory[i]) : HUDInfo.getItemStackCount(mc.thePlayer.inventory.armorInventory[i], Integer.parseInt(itemCount)));
                leftArrowCountList.add(""); // dummy bow arrow count list size
            }
        }

        for (int i = 0; i <= 1; i++)
        {
            if (mc.thePlayer.inventory.armorInventory[i] != null)
            {
                String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mc.thePlayer.inventory.armorInventory[i]);
                rightItemStackList.add(mc.thePlayer.inventory.armorInventory[i]);
                rightItemStatusList.add(mc.thePlayer.inventory.armorInventory[i].isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mc.thePlayer.inventory.armorInventory[i]) : HUDInfo.getItemStackCount(mc.thePlayer.inventory.armorInventory[i], Integer.parseInt(itemCount)));
            }
        }

        if (mainHandItem != null)
        {
            leftItemStackList.add(mainHandItem);
            String itemCount = HUDInfo.getInventoryItemCount(mc.thePlayer.inventory, mainHandItem);
            leftItemStatusList.add(!(status.equals("count") || status.equals("count_and_stack")) && mainHandItem.isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mainHandItem) : status.equals("none") ? "" : HUDInfo.getItemStackCount(mainHandItem, Integer.parseInt(itemCount)));

            if (mainHandItem.getItem() == Items.bow)
            {
                leftArrowCountList.add(HUDInfo.getArrowStackCount(arrowCount));
            }
            else
            {
                leftArrowCountList.add(""); // dummy bow arrow count list size
            }
        }

        // left item render stuff
        for (int i = 0; i < leftItemStackList.size(); ++i)
        {
            ItemStack itemStack = leftItemStackList.get(i);

            if (!leftItemStackList.isEmpty())
            {
                int baseXOffset = res.getScaledWidth() / 2 - 91 - 20;
                int yOffset = res.getScaledHeight() - 16 * i - 18;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
            }
        }

        // right item render stuff
        for (int i = 0; i < rightItemStackList.size(); ++i)
        {
            ItemStack itemStack = rightItemStackList.get(i);

            if (!rightItemStackList.isEmpty())
            {
                int baseXOffset = res.getScaledWidth() / 2 + 95;
                int yOffset = res.getScaledHeight() - 16 * i - 18;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
            }
        }

        // left durability/item count stuff
        for (int i = 0; i < leftItemStatusList.size(); ++i)
        {
            String string = leftItemStatusList.get(i);
            int stringWidth = mc.fontRendererObj.getStringWidth(string);
            float xOffset = res.getScaledWidth() / 2 - 114 - stringWidth;
            int yOffset = res.getScaledHeight() - 16 * i - 14;
            mc.fontRendererObj.drawString(ColorUtils.stringToRGB(ExtendedConfig.instance.equipmentStatusColor).toColoredFont() + string, xOffset, yOffset, 16777215, true);
        }

        // right durability/item count stuff
        for (int i = 0; i < rightItemStatusList.size(); ++i)
        {
            String string = rightItemStatusList.get(i);
            float xOffset = res.getScaledWidth() / 2 + 114;
            int yOffset = res.getScaledHeight() - 16 * i - 14;
            mc.fontRendererObj.drawString(ColorUtils.stringToRGB(ExtendedConfig.instance.equipmentStatusColor).toColoredFont() + string, xOffset, yOffset, 16777215, true);
        }

        // left arrow count stuff
        for (int i = 0; i < leftArrowCountList.size(); ++i)
        {
            String string = leftArrowCountList.get(i);
            int stringWidth = mc.fontRendererObj.getStringWidth(string);
            float xOffset = res.getScaledWidth() / 2 - 90 - stringWidth;
            int yOffset = res.getScaledHeight() - 16 * i - 10;

            if (!string.isEmpty())
            {
                GlStateManager.disableDepth();
                ColorUtils.unicodeFontRenderer.drawString(ColorUtils.stringToRGB(ExtendedConfig.instance.arrowCountColor).toColoredFont() + string, xOffset, yOffset, 16777215, true);
                GlStateManager.enableDepth();
            }
        }
    }

    public static void renderPotionHUD(Minecraft mc)
    {
        boolean iconAndTime = EnumPotionStatus.Style.getById(ExtendedConfig.instance.potionHUDStyle).equalsIgnoreCase("icon_and_time");
        boolean right = EnumPotionStatus.Position.getById(ExtendedConfig.instance.potionHUDPosition).equalsIgnoreCase("right");
        boolean showIcon = ExtendedConfig.instance.potionHUDIcon;
        String potionPos = EnumPotionStatus.Position.getById(ExtendedConfig.instance.potionHUDPosition);
        ScaledResolution scaledRes = new ScaledResolution(mc);
        int size = ExtendedConfig.instance.maximumPotionDisplay;
        int length = ExtendedConfig.instance.potionLengthYOffset;
        int lengthOverlap = ExtendedConfig.instance.potionLengthYOffsetOverlap;
        Collection<PotionEffect> collection = mc.thePlayer.getActivePotionEffects();
        int xPotion = 0;
        int yPotion = 0;

        if (potionPos.equalsIgnoreCase("hotbar_left"))
        {
            xPotion = scaledRes.getScaledWidth() / 2 - 91 - 35;
            yPotion = scaledRes.getScaledHeight() - 46;
        }
        else if (potionPos.equalsIgnoreCase("hotbar_right"))
        {
            xPotion = scaledRes.getScaledWidth() / 2 + 91 - 20;
            yPotion = scaledRes.getScaledHeight() - 42;
        }
        else
        {
            xPotion = right ? scaledRes.getScaledWidth() - 32 : -24;
            yPotion = scaledRes.getScaledHeight() - 220 + ExtendedConfig.instance.potionHUDYOffset + 90;
        }

        if (!collection.isEmpty())
        {
            if (collection.size() > size)
            {
                length = lengthOverlap / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : mc.thePlayer.getActivePotionEffects())
            {
                float alpha = 1.0F;
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                String s = Potion.getDurationString(potioneffect);
                String s1 = LangUtils.translate(potion.getName());

                if (!potioneffect.getIsAmbient() && potioneffect.getDuration() <= 200)
                {
                    int j1 = 10 - potioneffect.getDuration() / 20;
                    alpha = MathHelper.clamp_float(potioneffect.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos(potioneffect.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp_float(j1 / 10.0F * 0.25F, 0.0F, 0.25F);
                }

                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

                if (showIcon)
                {
                    mc.getTextureManager().bindTexture(HUDInfo.inventoryBackground);
                    int index = potion.getStatusIconIndex();

                    if (potionPos.equalsIgnoreCase("hotbar_left"))
                    {
                        mc.ingameGUI.drawTexturedModalRect(xPotion + 12, yPotion + 6, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                    }
                    else if (potionPos.equalsIgnoreCase("hotbar_right"))
                    {
                        mc.ingameGUI.drawTexturedModalRect(xPotion + 24, yPotion + 6, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                    }
                    else
                    {
                        mc.ingameGUI.drawTexturedModalRect(right ? xPotion + 12 : xPotion + 28, yPotion + 6, index % 8 * 18, 198 + index / 8 * 18, 18, 18);
                    }
                }

                if (potioneffect.getAmplifier() == 1)
                {
                    s1 = s1 + " " + LangUtils.translate("enchantment.level.2");
                }
                else if (potioneffect.getAmplifier() == 2)
                {
                    s1 = s1 + " " + LangUtils.translate("enchantment.level.3");
                }
                else if (potioneffect.getAmplifier() == 3)
                {
                    s1 = s1 + " " + LangUtils.translate("enchantment.level.4");
                }

                int stringwidth1 = mc.fontRendererObj.getStringWidth(s);
                int stringwidth2 = mc.fontRendererObj.getStringWidth(s1);

                if (potionPos.equalsIgnoreCase("hotbar_left"))
                {
                    if (!iconAndTime)
                    {
                        mc.fontRendererObj.drawString(s1, showIcon ? xPotion + 8 - stringwidth2 : xPotion + 28 - stringwidth2, yPotion + 6, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                    }
                    mc.fontRendererObj.drawString(s, showIcon ? xPotion + 8 - stringwidth1 : xPotion + 28 - stringwidth1, iconAndTime ? yPotion + 11 : yPotion + 16, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                }
                else if (potionPos.equalsIgnoreCase("hotbar_right"))
                {
                    if (!iconAndTime)
                    {
                        mc.fontRendererObj.drawString(s1, showIcon ? xPotion + 46 : xPotion + 28, yPotion + 6, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                    }
                    mc.fontRendererObj.drawString(s, showIcon ? xPotion + 46 : xPotion + 28, iconAndTime ? yPotion + 11 : yPotion + 16, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                }
                else
                {
                    if (!iconAndTime)
                    {
                        mc.fontRendererObj.drawString(s1, right ? showIcon ? xPotion + 8 - stringwidth2 : xPotion + 28 - stringwidth2 : showIcon ? xPotion + 50 : xPotion + 28, yPotion + 6, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                    }
                    mc.fontRendererObj.drawString(s, right ? showIcon ? xPotion + 8 - stringwidth1 : xPotion + 28 - stringwidth1 : showIcon ? xPotion + 50 : xPotion + 28, iconAndTime ? yPotion + 11 : yPotion + 16, ExtendedConfig.instance.alternatePotionHUDTextColor ? potion.getLiquidColor() : 16777215, true);
                }
                yPotion -= length;
            }
        }
    }

    static String getArmorDurabilityStatus(ItemStack itemStack)
    {
        String status = EnumEquipment.Status.getById(ExtendedConfig.instance.equipmentStatus);

        switch (status)
        {
        case "damage/max_damage":
        default:
            return itemStack.getMaxDamage() - itemStack.getItemDamage() + "/" + itemStack.getMaxDamage();
        case "percent":
            return HUDInfo.calculateItemDurabilityPercent(itemStack) + "%";
        case "damage":
            return String.valueOf(itemStack.getMaxDamage() - itemStack.getItemDamage());
        case "none":
        case "count":
        case "count_and_stack":
            return "";
        }
    }

    private static int calculateItemDurabilityPercent(ItemStack itemStack)
    {
        return itemStack.getMaxDamage() <= 0 ? 0 : 100 - itemStack.getItemDamage() * 100 / itemStack.getMaxDamage();
    }

    private static String getResponseTimeColor(int responseTime)
    {
        if (responseTime >= 200 && responseTime < 300)
        {
            return ColorUtils.stringToRGB(ExtendedConfig.instance.ping200And300Color).toColoredFont();
        }
        else if (responseTime >= 300 && responseTime < 500)
        {
            return ColorUtils.stringToRGB(ExtendedConfig.instance.ping300And500Color).toColoredFont();
        }
        else if (responseTime >= 500)
        {
            return ColorUtils.stringToRGB(ExtendedConfig.instance.pingMax500Color).toColoredFont();
        }
        else
        {
            return ColorUtils.stringToRGB(ExtendedConfig.instance.pingValueColor).toColoredFont();
        }
    }

    public static void renderItem(ItemStack itemStack, int x, int y)
    {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();

        if (itemStack.isItemStackDamageable())
        {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.disableLighting();
            GlStateManager.enableCull();
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, itemStack, x, y);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.disableLighting();
        }
    }

    static String getInventoryItemCount(InventoryPlayer inventory, ItemStack other)
    {
        String status = EnumEquipment.Status.getById(ExtendedConfig.instance.equipmentStatus);
        int count = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack playerItems = inventory.getStackInSlot(i);

            if (playerItems == null)
            {
                continue;
            }
            if (other.isItemStackDamageable() && (status.equals("count") || status.equals("count_and_stack")))
            {
                break;
            }

            if (playerItems.getItem() == other.getItem() && playerItems.getItemDamage() == other.getItemDamage() && ItemStack.areItemStackTagsEqual(playerItems, other))
            {
                count += playerItems.stackSize;
            }
        }
        return String.valueOf(count);
    }

    static int getInventoryArrowCount(InventoryPlayer inventory)
    {
        int arrowCount = 0;

        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            ItemStack itemStack = inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getItem() == Items.arrow)
            {
                arrowCount += itemStack.stackSize;
            }
        }
        return arrowCount;
    }

    static String getItemStackCount(ItemStack itemStack, int count)
    {
        String status = EnumEquipment.Status.getById(ExtendedConfig.instance.equipmentStatus);
        double stack = count / (double)itemStack.getMaxStackSize();
        int stackInt = count / itemStack.getMaxStackSize();
        String stackText = String.format("%.2f", stack);

        if (DoubleMath.isMathematicalInteger(stack))
        {
            stackText = String.valueOf(stackInt);
        }
        return count == 0 || count == 1 || count == 1 && itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean("Unbreakable") ? "" : String.valueOf(status.equals("count_and_stack") ? count + "/" + stackText : count);
    }

    static String getArrowStackCount(int count)
    {
        return count == 0 ? "" : String.valueOf(count);
    }
}