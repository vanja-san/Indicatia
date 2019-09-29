package stevekung.mods.indicatia.utils;

import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;

public class InfoUtils
{
    public static final InfoUtils INSTANCE = new InfoUtils();
    private final java.util.Timer timer = new java.util.Timer();
    public Entity extendedPointedEntity;
    private Entity pointedEntity;

    public int getPing()
    {
        NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(Minecraft.getMinecraft().thePlayer.getUniqueID());

        if (info != null)
        {
            if (info.getResponseTime() > 0)
            {
                return info.getResponseTime();
            }
            else
            {
                return IndicatiaEventHandler.currentServerPing;
            }
        }
        return 0;
    }

    public boolean isHypixel()
    {
        ServerData server = Minecraft.getMinecraft().getCurrentServerData();

        if (server != null)
        {
            Pattern pattern = Pattern.compile("^(?:(?:(?:.*\\.)?hypixel\\.net)|(?:209\\.222\\.115\\.\\d{1,3}))(?::\\d{1,5})?$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(server.serverIP);
            return matcher.find();
        }
        return false;
    }

    public String getCurrentGameTime(long worldTicks)
    {
        int hours = (int)((worldTicks / 1000 + 6) % 24);
        int minutes = (int)(60 * (worldTicks % 1000) / 1000);
        String sminutes = "" + minutes;
        String shours = "" + hours;
        String ampm = hours >= 12 ? "PM" : "AM";

        if (hours <= 9)
        {
            shours = 0 + "" + hours;
        }
        if (minutes <= 9)
        {
            sminutes = 0 + "" + minutes;
        }
        return ColorUtils.stringToRGB(ExtendedConfig.instance.gameTimeColor).toColoredFont() + "Game: " + ColorUtils.stringToRGB(ExtendedConfig.instance.gameTimeValueColor).toColoredFont() + shours + ":" + sminutes + " " + ampm;
    }

    public String getMoonPhase(Minecraft mc)
    {
        int[] moonPhaseFactors = { 4, 3, 2, 1, 0, -1, -2, -3 };
        int phase = moonPhaseFactors[mc.theWorld.provider.getMoonPhase(mc.theWorld.getWorldTime())];
        String status;

        switch (phase)
        {
        case 4:
        default:
            status = "Full Moon";
            break;
        case 3:
            status = "Waning Gibbous";
            break;
        case 2:
            status = "Last Quarter";
            break;
        case 1:
            status = "Waning Crescent";
            break;
        case 0:
            status = "New Moon";
            break;
        case -1:
            status = "Waxing Crescent";
            break;
        case -2:
            status = "First Quarter";
            break;
        case -3:
            status = "Waxing Gibbous";
            break;
        }
        return ColorUtils.stringToRGB(ExtendedConfig.instance.moonPhaseColor).toColoredFont() + "Moon Phase: " + ColorUtils.stringToRGB(ExtendedConfig.instance.moonPhaseValueColor).toColoredFont() + status;
    }

    public int parseInt(String input, String type)
    {
        try
        {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e)
        {
            Minecraft.getMinecraft().thePlayer.addChatMessage(JsonUtils.create(LangUtils.translate("commands.generic.num.invalid", input) + " in " + type + " setting").setChatStyle(JsonUtils.red()));
            return 0;
        }
    }

    public void processMouseOverEntity(Minecraft mc)
    {
        float partialTicks = mc.timer.renderPartialTicks;
        Entity entity = mc.getRenderViewEntity();
        double distance = 12.0D;

        if (entity != null && mc.theWorld != null)
        {
            this.extendedPointedEntity = null;
            mc.objectMouseOver = entity.rayTrace(distance, partialTicks);
            Vec3 vec3d = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            double d1 = distance;

            if (mc.playerController.extendedReach())
            {
                d1 = distance;
                distance = d1;
            }
            else
            {
                if (distance > distance)
                {
                    flag = true;
                }
            }

            if (mc.objectMouseOver != null)
            {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3d);
            }

            Vec3 vec3d1 = entity.getLook(1.0F);
            Vec3 vec3d2 = vec3d.addVector(vec3d1.xCoord * distance, vec3d1.yCoord * distance, vec3d1.zCoord * distance);
            this.pointedEntity = null;
            Vec3 vec3d3 = null;
            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(vec3d1.xCoord * distance, vec3d1.yCoord * distance, vec3d1.zCoord * distance).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, (Predicate<Entity>) entry -> entry != null && entry.canBeCollidedWith()));
            double d2 = d1;

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity1 = list.get(j);
                float size = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(size, size, size);
                MovingObjectPosition raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

                if (axisalignedbb.isVecInside(vec3d))
                {
                    if (d2 >= 0.0D)
                    {
                        this.pointedEntity = entity1;
                        vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                        d2 = 0.0D;
                    }
                }
                else if (raytraceresult != null)
                {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                    if (d3 < d2 || d2 == 0.0D)
                    {
                        if (entity1 == entity.ridingEntity && !entity.canRiderInteract())
                        {
                            if (d2 == 0.0D)
                            {
                                this.pointedEntity = entity1;
                                vec3d3 = raytraceresult.hitVec;
                            }
                        }
                        else
                        {
                            this.pointedEntity = entity1;
                            vec3d3 = raytraceresult.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
            if (this.pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > distance)
            {
                this.pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec3d3, null, new BlockPos(vec3d3));
            }
            if (this.pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null))
            {
                mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec3d3);

                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame)
                {
                    this.extendedPointedEntity = this.pointedEntity;
                }
            }
        }
    }

    public TimerTask schedule(Runnable runnable, long delay)
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
        this.timer.schedule(task, delay);
        return task;
    }
}