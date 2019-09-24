package stevekung.mods.indicatia.gui.toasts;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToast extends Gui
{
    protected final Minecraft mc;
    private final GuiToast.ToastInstance<?>[] visible = new GuiToast.ToastInstance[5];
    private final Deque<IToast> toastsQueue = new ArrayDeque<>();

    public GuiToast(Minecraft mc)
    {
        this.mc = mc;
    }

    public void drawToast(ScaledResolution resolution)
    {
        if (!this.mc.gameSettings.hideGUI)
        {
            RenderHelper.disableStandardItemLighting();

            for (int i = 0; i < this.visible.length; ++i)
            {
                GuiToast.ToastInstance<?> toastinstance = this.visible[i];

                if (toastinstance != null && toastinstance.render(resolution.getScaledWidth(), i))
                {
                    this.visible[i] = null;
                }
                if (this.visible[i] == null && !this.toastsQueue.isEmpty())
                {
                    this.visible[i] = new GuiToast.ToastInstance(this.toastsQueue.removeFirst());
                }
            }
        }
    }

    @Nullable
    public <T extends IToast> T getToast(Class<? extends T> clazz, Object obj)
    {
        for (GuiToast.ToastInstance<?> ins : this.visible)
        {
            if (ins != null && clazz.isAssignableFrom(ins.getToast().getClass()) && ins.getToast().getType().equals(obj))
            {
                return (T)ins.getToast();
            }
        }
        for (IToast toast : this.toastsQueue)
        {
            if (clazz.isAssignableFrom(toast.getClass()) && toast.getType().equals(obj))
            {
                return (T)toast;
            }
        }
        return null;
    }

    public void clear()
    {
        Arrays.fill(this.visible, null);
        this.toastsQueue.clear();
    }

    public void add(IToast toast)
    {
        this.toastsQueue.add(toast);
    }

    @SideOnly(Side.CLIENT)
    class ToastInstance<T extends IToast>
    {
        private final T toast;
        private long animationTime;
        private long visibleTime;
        private IToast.Visibility visibility;

        private ToastInstance(T toast)
        {
            this.animationTime = -1L;
            this.visibleTime = -1L;
            this.visibility = IToast.Visibility.SHOW;
            this.toast = toast;
        }

        public T getToast()
        {
            return this.toast;
        }

        private float getVisibility(long delta)
        {
            float f = MathHelper.clamp_float((delta - this.animationTime) / 600.0F, 0.0F, 1.0F);
            f = f * f;
            return this.visibility == IToast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(int x, int z)
        {
            long i = Minecraft.getSystemTime();

            if (this.animationTime == -1L)
            {
                this.animationTime = i;
                this.visibility.playSound(GuiToast.this.mc.getSoundHandler());
            }
            if (this.visibility == IToast.Visibility.SHOW && i - this.animationTime <= 600L)
            {
                this.visibleTime = i;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(x - 160.0F * this.getVisibility(i), z * 32, 500 + z);
            IToast.Visibility itoast$visibility = this.toast.draw(GuiToast.this, i - this.visibleTime);
            GlStateManager.popMatrix();

            if (itoast$visibility != this.visibility)
            {
                this.animationTime = i - (int)((1.0F - this.getVisibility(i)) * 600.0F);
                this.visibility = itoast$visibility;
                this.visibility.playSound(GuiToast.this.mc.getSoundHandler());
            }
            return this.visibility == IToast.Visibility.HIDE && i - this.animationTime > 600L;
        }
    }
}