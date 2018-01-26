package stevekung.mods.indicatia.renderer;

import java.text.SimpleDateFormat;
import java.util.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import stevekung.mods.indicatia.config.ConfigManager;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.handler.CommonHandler;
import stevekung.mods.indicatia.util.InfoUtil;
import stevekung.mods.indicatia.util.LangUtil;

public class HUDInfo
{
    private static final RenderItem renderItem = new RenderItem();
    private static final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");

    public static String getFPS()
    {
        int fps = Minecraft.debugFPS;
        String color = ColoredFontRenderer.color(ExtendedConfig.FPS_M40_COLOR_R, ExtendedConfig.FPS_M40_COLOR_G, ExtendedConfig.FPS_M40_COLOR_B);

        if (fps > 25 && fps <= 40)
        {
            color = ColoredFontRenderer.color(ExtendedConfig.FPS_26_40_COLOR_R, ExtendedConfig.FPS_26_40_COLOR_G, ExtendedConfig.FPS_26_40_COLOR_B);
        }
        else if (fps <= 25)
        {
            color = ColoredFontRenderer.color(ExtendedConfig.FPS_L25_COLOR_R, ExtendedConfig.FPS_L25_COLOR_G, ExtendedConfig.FPS_L25_COLOR_B);
        }
        return ColoredFontRenderer.color(ExtendedConfig.FPS_COLOR_R, ExtendedConfig.FPS_COLOR_G, ExtendedConfig.FPS_COLOR_B) + "FPS: " + color + fps;
    }

    public static String getXYZ(Minecraft mc)
    {
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.boundingBox.minY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);
        String nether = mc.thePlayer.dimension == -1 ? "Nether " : "";
        return ColoredFontRenderer.color(ExtendedConfig.XYZ_COLOR_R, ExtendedConfig.XYZ_COLOR_G, ExtendedConfig.XYZ_COLOR_B) + nether + "XYZ: " + ColoredFontRenderer.color(ExtendedConfig.XYZ_VALUE_COLOR_R, ExtendedConfig.XYZ_VALUE_COLOR_G, ExtendedConfig.XYZ_VALUE_COLOR_B) + x + " " + y + " " + z;
    }

    public static String getOverworldXYZFromNether(Minecraft mc)
    {
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.boundingBox.minY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);
        return ColoredFontRenderer.color(ExtendedConfig.XYZ_COLOR_R, ExtendedConfig.XYZ_COLOR_G, ExtendedConfig.XYZ_COLOR_B) + "Overworld XYZ: " + ColoredFontRenderer.color(ExtendedConfig.XYZ_VALUE_COLOR_R, ExtendedConfig.XYZ_VALUE_COLOR_G, ExtendedConfig.XYZ_VALUE_COLOR_B) + x * 8 + " " + y + " " + z * 8;
    }

    public static String getBiome(Minecraft mc)
    {
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.posY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);

        if (mc.theWorld != null && mc.theWorld.blockExists(x, y, z))
        {
            Chunk chunk = mc.theWorld.getChunkFromBlockCoords(x, z);

            if (!chunk.isEmpty())
            {
                String biomeName = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, mc.theWorld.getWorldChunkManager()).biomeName.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
                return ColoredFontRenderer.color(ExtendedConfig.BIOME_COLOR_R, ExtendedConfig.BIOME_COLOR_G, ExtendedConfig.BIOME_COLOR_B) + "Biome: " + ColoredFontRenderer.color(ExtendedConfig.BIOME_VALUE_COLOR_R, ExtendedConfig.BIOME_VALUE_COLOR_G, ExtendedConfig.BIOME_VALUE_COLOR_B) + biomeName;
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
        int responseTime = CommonHandler.currentServerPing;
        return ColoredFontRenderer.color(ExtendedConfig.PING_COLOR_R, ExtendedConfig.PING_COLOR_G, ExtendedConfig.PING_COLOR_B) + "Ping: " + HUDInfo.getResponseTimeColor(responseTime) + responseTime + "ms";
    }

    public static String getPingToSecond()
    {
        double responseTime = CommonHandler.currentServerPing / 1000D;
        return ColoredFontRenderer.color(ExtendedConfig.PING_COLOR_R, ExtendedConfig.PING_COLOR_G, ExtendedConfig.PING_COLOR_B) + "Delay: " + HUDInfo.getResponseTimeColor((int) (responseTime * 1000D)) + responseTime + "s";
    }

    public static String getServerIP(Minecraft mc)
    {
        String ip = ColoredFontRenderer.color(ExtendedConfig.IP_COLOR_R, ExtendedConfig.IP_COLOR_G, ExtendedConfig.IP_COLOR_B) + "IP: " + "" + ColoredFontRenderer.color(ExtendedConfig.IP_VALUE_COLOR_R, ExtendedConfig.IP_VALUE_COLOR_G, ExtendedConfig.IP_VALUE_COLOR_B) + mc.func_147104_D().serverIP;

        if (ConfigManager.enableServerIPMCVersion)
        {
            ip = ip + "/" + IndicatiaMod.MC_VERSION;
        }
        return ip;
    }

    public static String renderDirection(Minecraft mc)
    {
        Entity entity = mc.renderViewEntity;
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
        return ColoredFontRenderer.color(ExtendedConfig.DIRECTION_COLOR_R, ExtendedConfig.DIRECTION_COLOR_G, ExtendedConfig.DIRECTION_COLOR_B) + "Direction: " + ColoredFontRenderer.color(ExtendedConfig.DIRECTION_VALUE_COLOR_R, ExtendedConfig.DIRECTION_VALUE_COLOR_G, ExtendedConfig.DIRECTION_VALUE_COLOR_B) + direction;
    }

    public static String getCPS()
    {
        return ColoredFontRenderer.color(ExtendedConfig.CPS_COLOR_R, ExtendedConfig.CPS_COLOR_G, ExtendedConfig.CPS_COLOR_B) + "CPS: " + "" + ColoredFontRenderer.color(ExtendedConfig.CPS_VALUE_COLOR_R, ExtendedConfig.CPS_VALUE_COLOR_G, ExtendedConfig.CPS_VALUE_COLOR_B) + InfoUtil.INSTANCE.getCPS();
    }

    public static String getRCPS()
    {
        return ColoredFontRenderer.color(ExtendedConfig.RCPS_COLOR_R, ExtendedConfig.RCPS_COLOR_G, ExtendedConfig.RCPS_COLOR_B) + "RCPS: " + "" + ColoredFontRenderer.color(ExtendedConfig.RCPS_VALUE_COLOR_R, ExtendedConfig.RCPS_VALUE_COLOR_G, ExtendedConfig.RCPS_VALUE_COLOR_B) + InfoUtil.INSTANCE.getRCPS();
    }

    public static String getCurrentTime()
    {
        String currentTime = new SimpleDateFormat("d/M/yyyy HH:mm:ss a", Locale.ENGLISH).format(new Date());
        return currentTime = "Time: " + currentTime.replace("\u0e2b\u0e25\u0e31\u0e07\u0e40\u0e17\u0e35\u0e48\u0e22\u0e07", "PM").replace("\u0e01\u0e48\u0e2d\u0e19\u0e40\u0e17\u0e35\u0e48\u0e22\u0e07", "AM");
    }

    public static String getCurrentGameTime(Minecraft mc)
    {
        return InfoUtil.INSTANCE.getCurrentGameTime(mc.theWorld.getWorldTime() % 24000);
    }

    public static String getGameWeather(Minecraft mc)
    {
        String weather = mc.theWorld.isRaining() && !mc.theWorld.isThundering() ? "Raining" : mc.theWorld.isRaining() && mc.theWorld.isThundering() ? "Thunder" : "";
        return "Weather: " + weather;
    }

    public static void renderHorizontalEquippedItems(Minecraft mc)
    {
        String ordering = ConfigManager.equipmentOrdering;
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        boolean isRightSide = ConfigManager.equipmentPosition.equals("right");
        int baseXOffset = 2;
        int baseYOffset = ExtendedConfig.ARMOR_STATUS_OFFSET;
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
        String ordering = ConfigManager.equipmentOrdering;
        String status = ConfigManager.equipmentStatus;
        List<ItemStack> itemStackList = new ArrayList<>();
        List<String> itemStatusList = new ArrayList<>();
        List<String> arrowCountList = new ArrayList<>();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        boolean isRightSide = ConfigManager.equipmentPosition.equals("right");
        int baseXOffset = isRightSide ? res.getScaledWidth() - 18 : 2;
        int baseYOffset = ExtendedConfig.ARMOR_STATUS_OFFSET;
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
            mc.mcProfiler.startSection("item_stack_render");

            if (!itemStackList.isEmpty())
            {
                int yOffset = baseYOffset + 16 * i;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
                yOffset += 16;
            }
            mc.mcProfiler.endSection();
        }

        float yOffset = 0;
        float fontHeight = 0;

        // durability/item count stuff
        for (int i = 0; i < itemStatusList.size(); ++i)
        {
            String string = itemStatusList.get(i);
            fontHeight = IndicatiaMod.coloredFontRenderer.FONT_HEIGHT + 7.0625F;

            if (!string.isEmpty())
            {
                yOffset = baseYOffset + 4 + fontHeight * i;
                mc.mcProfiler.startSection("armor_durability_info");
                float xOffset = isRightSide ? res.getScaledWidth() - mc.fontRenderer.getStringWidth(string) - 20.0625F : baseXOffset + 18.0625F;
                IndicatiaMod.coloredFontRenderer.drawString(ColoredFontRenderer.color(ExtendedConfig.EQUIPMENT_COLOR_R, ExtendedConfig.EQUIPMENT_COLOR_G, ExtendedConfig.EQUIPMENT_COLOR_B) + string, (int) xOffset, (int) yOffset, 16777215, true);
                mc.mcProfiler.endSection();
            }
        }

        // arrow count stuff
        for (int i = 0; i < arrowCountList.size(); ++i)
        {
            String string = arrowCountList.get(i);
            yOffset = baseYOffset + 8 + fontHeight * i;

            if (!string.isEmpty())
            {
                mc.mcProfiler.startSection("arrow_count");
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                IndicatiaMod.coloredFontRenderer.setUnicodeFlag(true);
                IndicatiaMod.coloredFontRenderer.drawString(ColoredFontRenderer.color(ExtendedConfig.ARROW_COUNT_COLOR_R, ExtendedConfig.ARROW_COUNT_COLOR_G, ExtendedConfig.ARROW_COUNT_COLOR_B) + string, (int) (isRightSide ? res.getScaledWidth() - mc.fontRenderer.getStringWidth(string) - 2.0625F : baseXOffset + 8.0625F), (int)yOffset, 16777215, true);
                IndicatiaMod.coloredFontRenderer.setUnicodeFlag(false);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                mc.mcProfiler.endSection();
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
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        ItemStack mainHandItem = mc.thePlayer.getHeldItem();
        int arrowCount = HUDInfo.getInventoryArrowCount(mc.thePlayer.inventory);
        String status = ConfigManager.equipmentStatus;

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
            leftItemStatusList.add(mainHandItem.isItemStackDamageable() ? HUDInfo.getArmorDurabilityStatus(mainHandItem) : status.equals("none") ? "" : HUDInfo.getItemStackCount(mainHandItem, Integer.parseInt(itemCount)));

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
            mc.mcProfiler.startSection("item_stack_render");

            if (!leftItemStackList.isEmpty())
            {
                int baseXOffset = res.getScaledWidth() / 2 - 91 - 20;
                int yOffset = res.getScaledHeight() - 16 * i - 18;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
            }
            mc.mcProfiler.endSection();
        }

        // right item render stuff
        for (int i = 0; i < rightItemStackList.size(); ++i)
        {
            ItemStack itemStack = rightItemStackList.get(i);
            mc.mcProfiler.startSection("item_stack_render");

            if (!rightItemStackList.isEmpty())
            {
                int baseXOffset = res.getScaledWidth() / 2 + 95;
                int yOffset = res.getScaledHeight() - 16 * i - 18;
                HUDInfo.renderItem(itemStack, baseXOffset, yOffset);
            }
            mc.mcProfiler.endSection();
        }

        // left durability/item count stuff
        for (int i = 0; i < leftItemStatusList.size(); ++i)
        {
            String string = leftItemStatusList.get(i);
            mc.mcProfiler.startSection("armor_durability_info");
            int stringWidth = mc.fontRenderer.getStringWidth(string);
            float xOffset = res.getScaledWidth() / 2 - 114 - stringWidth;
            int yOffset = res.getScaledHeight() - 16 * i - 14;
            IndicatiaMod.coloredFontRenderer.drawString(ColoredFontRenderer.color(ExtendedConfig.EQUIPMENT_COLOR_R, ExtendedConfig.EQUIPMENT_COLOR_G, ExtendedConfig.EQUIPMENT_COLOR_B) + string, (int) xOffset, yOffset, 16777215, true);
            mc.mcProfiler.endSection();
        }

        // right durability/item count stuff
        for (int i = 0; i < rightItemStatusList.size(); ++i)
        {
            String string = rightItemStatusList.get(i);
            mc.mcProfiler.startSection("armor_durability_info");
            float xOffset = res.getScaledWidth() / 2 + 114;
            int yOffset = res.getScaledHeight() - 16 * i - 14;
            IndicatiaMod.coloredFontRenderer.drawString(ColoredFontRenderer.color(ExtendedConfig.EQUIPMENT_COLOR_R, ExtendedConfig.EQUIPMENT_COLOR_G, ExtendedConfig.EQUIPMENT_COLOR_B) + string, (int) xOffset, yOffset, 16777215, true);
            mc.mcProfiler.endSection();
        }

        // left arrow count stuff
        for (int i = 0; i < leftArrowCountList.size(); ++i)
        {
            String string = leftArrowCountList.get(i);
            int stringWidth = mc.fontRenderer.getStringWidth(string);
            float xOffset = res.getScaledWidth() / 2 - 90 - stringWidth;
            int yOffset = res.getScaledHeight() - 16 * i - 10;

            if (!string.isEmpty())
            {
                mc.mcProfiler.startSection("arrow_count");
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                IndicatiaMod.coloredFontRenderer.setUnicodeFlag(true);
                IndicatiaMod.coloredFontRenderer.drawString(ColoredFontRenderer.color(ExtendedConfig.ARROW_COUNT_COLOR_R, ExtendedConfig.ARROW_COUNT_COLOR_G, ExtendedConfig.ARROW_COUNT_COLOR_B) + string, (int)xOffset, yOffset, 16777215, true);
                IndicatiaMod.coloredFontRenderer.setUnicodeFlag(false);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                mc.mcProfiler.endSection();
            }
        }
    }

    public static void renderPotionStatusHUD(Minecraft mc)
    {
        if (ConfigManager.enablePotionStatusHUD)
        {
            boolean iconAndTime = ConfigManager.potionStatusHUDStyle.equals("icon_and_time");
            boolean showIcon = ConfigManager.enablePotionHUDIcon;
            ScaledResolution scaledRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int size = ExtendedConfig.MAX_POTION_DISPLAY;
            int length = ExtendedConfig.POTION_LENGTH_Y_OFFSET;
            int lengthOverlap = ExtendedConfig.POTION_LENGTH_Y_OFFSET_OVERLAP;
            Collection collection = mc.thePlayer.getActivePotionEffects();

            if (ConfigManager.potionStatusHUDPosition.equals("hotbar_left"))
            {
                int xPotion = scaledRes.getScaledWidth() / 2 - 91 - 35;
                int yPotion = scaledRes.getScaledHeight() - 46;

                if (!collection.isEmpty())
                {
                    if (collection.size() > size)
                    {
                        length = lengthOverlap / (collection.size() - 1);
                    }

                    for (Iterator iterator = mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext();)
                    {
                        PotionEffect potioneffect = (PotionEffect) iterator.next();
                        Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                        String s = Potion.getDurationString(potioneffect);
                        String s1 = LangUtil.translate(potion.getName());
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                        if (showIcon)
                        {
                            mc.getTextureManager().bindTexture(HUDInfo.inventoryBackground);
                            int i1 = potion.getStatusIconIndex();
                            mc.ingameGUI.drawTexturedModalRect(xPotion + 12, yPotion + 6, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        }

                        if (potioneffect.getAmplifier() == 1)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.2");
                        }
                        else if (potioneffect.getAmplifier() == 2)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.3");
                        }
                        else if (potioneffect.getAmplifier() == 3)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.4");
                        }
                        int stringwidth1 = IndicatiaMod.coloredFontRenderer.getStringWidth(s);
                        int stringwidth2 = IndicatiaMod.coloredFontRenderer.getStringWidth(s1);

                        if (!iconAndTime)
                        {
                            IndicatiaMod.coloredFontRenderer.drawString(s1, showIcon ? xPotion + 8 - stringwidth2 : xPotion + 28 - stringwidth2, yPotion + 6, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        }
                        IndicatiaMod.coloredFontRenderer.drawString(s, showIcon ? xPotion + 8 - stringwidth1 : xPotion + 28 - stringwidth1, iconAndTime ? yPotion + 11 : yPotion + 16, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        yPotion -= length;
                    }
                }
            }
            else if (ConfigManager.potionStatusHUDPosition.equals("hotbar_right"))
            {
                int xPotion = scaledRes.getScaledWidth() / 2 + 91 - 20;
                int yPotion = scaledRes.getScaledHeight() - 42;

                if (!collection.isEmpty())
                {
                    if (collection.size() > size)
                    {
                        length = lengthOverlap / (collection.size() - 1);
                    }

                    for (Iterator iterator = mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext();)
                    {
                        PotionEffect potioneffect = (PotionEffect) iterator.next();
                        Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                        String s = Potion.getDurationString(potioneffect);
                        String s1 = LangUtil.translate(potion.getName());
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                        if (showIcon)
                        {
                            mc.getTextureManager().bindTexture(HUDInfo.inventoryBackground);
                            int i1 = potion.getStatusIconIndex();
                            mc.ingameGUI.drawTexturedModalRect(xPotion + 24, yPotion + 6, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        }

                        if (potioneffect.getAmplifier() == 1)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.2");
                        }
                        else if (potioneffect.getAmplifier() == 2)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.3");
                        }
                        else if (potioneffect.getAmplifier() == 3)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.4");
                        }

                        if (!iconAndTime)
                        {
                            IndicatiaMod.coloredFontRenderer.drawString(s1, showIcon ? xPotion + 46 : xPotion + 28, yPotion + 6, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        }
                        IndicatiaMod.coloredFontRenderer.drawString(s, showIcon ? xPotion + 46 : xPotion + 28, iconAndTime ? yPotion + 11 : yPotion + 16, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        yPotion -= length;
                    }
                }
            }
            else
            {
                boolean right = ConfigManager.potionStatusHUDPosition.equals("right");
                int xPotion = right ? scaledRes.getScaledWidth() - 32 : -24;
                int yPotion = scaledRes.getScaledHeight() - 220 + ExtendedConfig.POTION_STATUS_OFFSET + 90;

                if (!collection.isEmpty())
                {
                    if (collection.size() > size)
                    {
                        length = lengthOverlap / (collection.size() - 1);
                    }

                    for (Iterator iterator = mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext();)
                    {
                        PotionEffect potioneffect = (PotionEffect) iterator.next();
                        Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                        String s = Potion.getDurationString(potioneffect);
                        String s1 = LangUtil.translate(potion.getName());
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                        if (showIcon)
                        {
                            mc.getTextureManager().bindTexture(HUDInfo.inventoryBackground);
                            int i1 = potion.getStatusIconIndex();
                            mc.ingameGUI.drawTexturedModalRect(right ? xPotion + 12 : xPotion + 28, yPotion + 6, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                        }

                        if (potioneffect.getAmplifier() == 1)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.2");
                        }
                        else if (potioneffect.getAmplifier() == 2)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.3");
                        }
                        else if (potioneffect.getAmplifier() == 3)
                        {
                            s1 = s1 + " " + LangUtil.translate("enchantment.level.4");
                        }

                        int stringwidth1 = IndicatiaMod.coloredFontRenderer.getStringWidth(s);
                        int stringwidth2 = IndicatiaMod.coloredFontRenderer.getStringWidth(s1);

                        if (!iconAndTime)
                        {
                            IndicatiaMod.coloredFontRenderer.drawString(s1, right ? showIcon ? xPotion + 8 - stringwidth2 : xPotion + 28 - stringwidth2 : showIcon ? xPotion + 50 : xPotion + 28, yPotion + 6, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        }
                        IndicatiaMod.coloredFontRenderer.drawString(s, right ? showIcon ? xPotion + 8 - stringwidth1 : xPotion + 28 - stringwidth1 : showIcon ? xPotion + 50 : xPotion + 28, iconAndTime ? yPotion + 11 : yPotion + 16, ConfigManager.alternatePotionHUDTextColor ? InfoUtil.INSTANCE.getAlternatePotionHUDTextColor(potion) : 16777215, true);
                        yPotion += length;
                    }
                }
            }
        }
    }

    static String getArmorDurabilityStatus(ItemStack itemStack)
    {
        String status = ConfigManager.equipmentStatus;

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
            return ColoredFontRenderer.color(ExtendedConfig.PING_200_300_COLOR_R, ExtendedConfig.PING_200_300_COLOR_G, ExtendedConfig.PING_200_300_COLOR_B);
        }
        else if (responseTime >= 300 && responseTime < 500)
        {
            return ColoredFontRenderer.color(ExtendedConfig.PING_300_500_COLOR_R, ExtendedConfig.PING_300_500_COLOR_G, ExtendedConfig.PING_300_500_COLOR_B);
        }
        else if (responseTime >= 500)
        {
            return ColoredFontRenderer.color(ExtendedConfig.PING_M500_COLOR_R, ExtendedConfig.PING_M500_COLOR_G, ExtendedConfig.PING_M500_COLOR_B);
        }
        else
        {
            return ColoredFontRenderer.color(ExtendedConfig.PING_L200_COLOR_R, ExtendedConfig.PING_L200_COLOR_G, ExtendedConfig.PING_L200_COLOR_B);
        }
    }

    static void renderItem(ItemStack itemStack, int x, int y)
    {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        HUDInfo.renderItem.renderItemAndEffectIntoGUI(IndicatiaMod.coloredFontRenderer, IndicatiaMod.MC.getTextureManager(), itemStack, x, y);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();

        if (itemStack.isItemStackDamageable())
        {
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_CULL_FACE);
            HUDInfo.renderItem.renderItemOverlayIntoGUI(IndicatiaMod.coloredFontRenderer, IndicatiaMod.MC.getTextureManager(), itemStack, x, y);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        GL11.glBlendFunc(770, 771);
    }

    static String getInventoryItemCount(InventoryPlayer inventory, ItemStack other)
    {
        int count = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack playerItems = inventory.getStackInSlot(i);

            if (playerItems != null && playerItems.getItem() == other.getItem() && playerItems.getItemDamage() == other.getItemDamage() && ItemStack.areItemStackTagsEqual(playerItems, other))
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
        return count == 0 || count == 1 || count == 1 && itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean("Unbreakable") ? "" : String.valueOf(count);
    }

    static String getArrowStackCount(int count)
    {
        return count == 0 ? "" : String.valueOf(count);
    }
}