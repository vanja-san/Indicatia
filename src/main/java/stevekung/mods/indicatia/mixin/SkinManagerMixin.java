package stevekung.mods.indicatia.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import stevekung.mods.indicatia.utils.CommonUtils;

@Mixin(SkinManager.class)
public abstract class SkinManagerMixin
{
    @Shadow
    @Final
    private MinecraftSessionService sessionService;

    private final SkinManager that = (SkinManager) (Object) this;

    @Overwrite
    public void loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure)
    {
        CommonUtils.POOL.execute(() ->
        {
            final Map<Type, MinecraftProfileTexture> map = new HashMap<>();

            try
            {
                map.putAll(this.sessionService.getTextures(profile, requireSecure));
            }
            catch (InsecureTextureException e) {}

            if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId()))
            {
                profile.getProperties().clear();
                profile.getProperties().putAll(Minecraft.getMinecraft().func_181037_M());
                map.putAll(this.sessionService.getTextures(profile, false));
            }

            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                if (map.containsKey(Type.SKIN))
                {
                    this.that.loadSkin(map.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                }
                if (map.containsKey(Type.CAPE))
                {
                    this.that.loadSkin(map.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                }
            });
        });
    }
}