package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.event.HypixelEventHandler;
import stevekung.mods.indicatia.utils.SkyBlockLocation;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin
{
    @Inject(method = "renderDebugBoundingBox(Lnet/minecraft/entity/Entity;DDDFF)V", cancellable = true, at = @At("HEAD"))
    private void renderDebugBoundingBox(Entity entity, double x, double y, double z, float f1, float partialTicks, CallbackInfo info)
    {
        if (ExtendedConfig.instance.showDragonHitboxOnly && !(entity instanceof EntityDragon) && HypixelEventHandler.isSkyBlock && HypixelEventHandler.SKY_BLOCK_LOCATION == SkyBlockLocation.DRAGON_NEST)
        {
            info.cancel();
        }
    }
}