package com.stevekung.indicatia.gui.exconfig;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.stevekung.indicatia.config.ExtendedConfig;
import com.stevekung.indicatia.gui.exconfig.screen.widget.ConfigOptionSliderWidget;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;

public class DoubleConfigOption extends ExtendedConfigOption
{
    protected final float interval;
    protected final double min;
    protected double max;
    private final Function<ExtendedConfig, Double> getter;
    private final BiConsumer<ExtendedConfig, Double> setter;
    private final BiFunction<ExtendedConfig, DoubleConfigOption, String> displayStringGetter;

    public DoubleConfigOption(String key, double min, double max, float interval, Function<ExtendedConfig, Double> getter, BiConsumer<ExtendedConfig, Double> setter, BiFunction<ExtendedConfig, DoubleConfigOption, String> displayStringGetter)
    {
        super(key);
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.getter = getter;
        this.setter = setter;
        this.displayStringGetter = displayStringGetter;
    }

    @Override
    public Widget createOptionButton(int x, int y, int width)
    {
        return new ConfigOptionSliderWidget(x, y, width, 20, this);
    }

    public double normalizeValue(double value)
    {
        return MathHelper.clamp((this.snapToStep(value) - this.min) / (this.max - this.min), 0.0D, 1.0D);
    }

    public double denormalizeValue(double value)
    {
        return this.snapToStep(MathHelper.lerp(MathHelper.clamp(value, 0.0D, 1.0D), this.min, this.max));
    }

    private double snapToStep(double value)
    {
        if (this.interval > 0.0F)
        {
            value = this.interval * Math.round(value / this.interval);
        }
        return MathHelper.clamp(value, this.min, this.max);
    }

    public double getMin()
    {
        return this.min;
    }

    public double getMax()
    {
        return this.max;
    }

    public void setMax(float value)
    {
        this.max = value;
    }

    public void set(double value)
    {
        this.setter.accept(ExtendedConfig.INSTANCE, value);
    }

    public double get()
    {
        return this.getter.apply(ExtendedConfig.INSTANCE);
    }

    public String getDisplayString()
    {
        return this.displayStringGetter.apply(ExtendedConfig.INSTANCE, this);
    }
}