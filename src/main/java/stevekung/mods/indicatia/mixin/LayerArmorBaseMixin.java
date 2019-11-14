package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.event.HypixelEventHandler;

@Mixin(LayerArmorBase.class)
public abstract class LayerArmorBaseMixin implements LayerRenderer<EntityLivingBase>
{
    @Shadow
    @Final
    private RendererLivingEntity<?> renderer;

    @Shadow
    protected abstract void func_177179_a(ModelBase p_177179_1_, int p_177179_2_);

    @Shadow
    protected abstract ModelBase getArmorModelHook(EntityLivingBase entity, ItemStack itemStack, int slot, ModelBase model);

    @Shadow
    protected abstract void renderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, int armorSlot);

    private final LayerArmorBase that = (LayerArmorBase) (Object) this;

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.renderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 4);
        this.renderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 3);
        this.renderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 2);
        this.renderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 1);
        this.renderGlowingLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 3);
        this.renderGlowingLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 2);
        this.renderGlowingLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 1);
    }

    private void renderGlowingLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, int armorSlot)
    {
        if (!HypixelEventHandler.foundSkyBlockPack)
        {
            return;
        }

        ItemStack itemstack = this.that.getCurrentArmor(entity, armorSlot);

        if (itemstack != null && itemstack.getItem() instanceof ItemArmor)
        {
            if (!itemstack.hasTagCompound())
            {
                return;
            }

            ModelBase t = this.that.func_177175_a(armorSlot);
            t.setModelAttributes(this.renderer.getMainModel());
            t.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            t = this.getArmorModelHook(entity, itemstack, armorSlot, t);
            this.func_177179_a(t, armorSlot);
            ResourceLocation res = this.getArmorType(itemstack, armorSlot);

            if (res == null)
            {
                return;
            }

            this.renderer.bindTexture(this.getArmorType(itemstack, armorSlot));

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(1, 1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(!entity.isInvisible());
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 128.0F, 128.0F);
            GlStateManager.enableLighting();

            float time = entity.getEntityId() + entity.ticksExisted + partialTicks;
            float sin = (MathHelper.sin(time / 16) + 1F) / 1.5F + 0.15F;
            GlStateManager.color(sin, sin, sin, sin);
            t.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            int i = entity.getBrightnessForRender(partialTicks);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return ConfigManagerIN.enableOldArmorRender;
    }

    private ResourceLocation getArmorType(ItemStack itemStack, int armorSlot)
    {
        String texture = "";
        String id = itemStack.getTagCompound().getCompoundTag("ExtraAttributes").getString("id");

        if (id.startsWith("SUPERIOR_DRAGON"))
        {
            texture = "superior";
        }
        else if (id.startsWith("WISE_DRAGON"))
        {
            texture = "wise";
        }
        else if (id.startsWith("YOUNG_DRAGON"))
        {
            texture = "young";
        }
        else if (id.startsWith("OLD_DRAGON"))
        {
            texture = "old";
        }
        else if (id.startsWith("PROTECTOR_DRAGON"))
        {
            texture = "protector";
        }
        else if (id.startsWith("UNSTABLE_DRAGON"))
        {
            texture = "unstable";
        }
        else if (id.startsWith("STRONG_DRAGON"))
        {
            texture = "strong";
        }

        String s1 = String.format("indicatia:textures/model/armor/%s_layer_%d.png", texture, armorSlot == 2 ? 2 : 1);
        return texture.isEmpty() ? null : new ResourceLocation(s1);
    }
}