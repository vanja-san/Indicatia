package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;

@Mixin(Item.class)
public abstract class ItemMixin
{
    @Inject(method = "shouldCauseReequipAnimation(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Z", cancellable = true, at = @At("HEAD"))
    private void disableReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged, CallbackInfoReturnable info)
    {
        boolean itemEqual = newStack != null && oldStack.getItem() == newStack.getItem();

        if (IndicatiaEventHandler.isSkyBlock && oldStack == null && newStack == null ? true : (oldStack != null && newStack != null ? itemEqual : false))
        {
            info.setReturnValue(false);
        }
    }
}