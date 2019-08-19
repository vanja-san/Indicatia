package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import stevekung.mods.indicatia.utils.CommonUtils;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer
{
    private final EntityPlayerSP that = (EntityPlayerSP) (Object) this;

    public EntityPlayerSPMixin(World world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "onLivingUpdate()V", cancellable = true, at = @At(value = "INVOKE", target = "net/minecraft/util/MovementInput.updatePlayerMoveState()V", shift = At.Shift.AFTER))
    private void updateMovementInput(CallbackInfo ci)
    {
        CommonUtils.onInputUpdate(this.that, this.that.movementInput);
    }
}