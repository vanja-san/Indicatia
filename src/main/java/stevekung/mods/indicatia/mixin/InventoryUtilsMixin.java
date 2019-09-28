package stevekung.mods.indicatia.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import stevekung.mods.indicatia.event.HUDRenderEventHandler;
import stevekung.mods.indicatia.event.HypixelEventHandler;
import stevekung.mods.indicatia.gui.toasts.ItemDropsToast;

@Mixin(value = InventoryUtils.class, remap = false)
public abstract class InventoryUtilsMixin
{
    @Inject(method = "getInventoryDifference([Lnet/minecraft/item/ItemStack;)V", remap = false, cancellable = true, at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.getDisplayName()Ljava/lang/String;", ordinal = 2, shift = Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void getInventoryDifference(ItemStack[] currentInventory, CallbackInfo info, List<ItemStack> newInventory, Map<String, Integer> previousInventoryMap, Map<String, Integer> newInventoryMap, int i, ItemStack newItem)
    {
        if (newItem != null)
        {
            if (HypixelEventHandler.rareDropName.equals(EnumChatFormatting.getTextWithoutFormattingCodes(newItem.getDisplayName())))
            {
                ItemDropsToast.addOrUpdate(HUDRenderEventHandler.INSTANCE.getToastGui(), newItem);
            }
        }
    }
}