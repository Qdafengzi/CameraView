package com.picupmedia.cameraview.filter;

import androidx.annotation.NonNull;

import com.picupmedia.cameraview.CameraView;
import com.picupmedia.cameraview.filters.AutoFixFilter;
import com.picupmedia.cameraview.filters.BlackAndWhiteFilter;
import com.picupmedia.cameraview.filters.BrightnessFilter;
import com.picupmedia.cameraview.filters.ContrastFilter;
import com.picupmedia.cameraview.filters.CrossProcessFilter;
import com.picupmedia.cameraview.filters.DocumentaryFilter;
import com.picupmedia.cameraview.filters.DuotoneFilter;
import com.picupmedia.cameraview.filters.FillLightFilter;
import com.picupmedia.cameraview.filters.GammaFilter;
import com.picupmedia.cameraview.filters.GrainFilter;
import com.picupmedia.cameraview.filters.GrayscaleFilter;
import com.picupmedia.cameraview.filters.HueFilter;
import com.picupmedia.cameraview.filters.InvertColorsFilter;
import com.picupmedia.cameraview.filters.LomoishFilter;
import com.picupmedia.cameraview.filters.PosterizeFilter;
import com.picupmedia.cameraview.filters.SaturationFilter;
import com.picupmedia.cameraview.filters.SepiaFilter;
import com.picupmedia.cameraview.filters.SharpnessFilter;
import com.picupmedia.cameraview.filters.TemperatureFilter;
import com.picupmedia.cameraview.filters.TintFilter;
import com.picupmedia.cameraview.filters.VignetteFilter;
import com.picupmedia.cameraview.filters.custom.ImageLevelFilter;
import com.picupmedia.cameraview.filters.custom.WhiteBalanceFilter;

/**
 * Contains commonly used {@link Filter}s.
 *
 * You can use {@link #newInstance()} to create a new instance and
 * pass it to {@link CameraView#setFilter(Filter)}.
 */
public enum Filters {

    /** @see NoFilter */
    NONE(NoFilter.class),

    /** @see AutoFixFilter */
    AUTO_FIX(AutoFixFilter.class),

    /** @see BlackAndWhiteFilter */
    BLACK_AND_WHITE(BlackAndWhiteFilter.class),

    /** @see BrightnessFilter */
    BRIGHTNESS(BrightnessFilter.class),

    /** @see ContrastFilter */
    CONTRAST(ContrastFilter.class),

    /** @see CrossProcessFilter */
    CROSS_PROCESS(CrossProcessFilter.class),

    /** @see DocumentaryFilter */
    DOCUMENTARY(DocumentaryFilter.class),

    /** @see DuotoneFilter */
    DUOTONE(DuotoneFilter.class),

    /** @see FillLightFilter */
    FILL_LIGHT(FillLightFilter.class),

    /** @see GammaFilter */
    GAMMA(GammaFilter.class),

    /** @see GrainFilter */
    GRAIN(GrainFilter.class),

    /** @see GrayscaleFilter */
    GRAYSCALE(GrayscaleFilter.class),

    /** @see HueFilter */
    HUE(HueFilter.class),

    /** @see InvertColorsFilter */
    INVERT_COLORS(InvertColorsFilter.class),

    /** @see LomoishFilter */
    LOMOISH(LomoishFilter.class),

    /** @see PosterizeFilter */
    POSTERIZE(PosterizeFilter.class),

    /** @see SaturationFilter */
    SATURATION(SaturationFilter.class),

    /** @see SepiaFilter */
    SEPIA(SepiaFilter.class),

    /** @see SharpnessFilter */
    SHARPNESS(SharpnessFilter.class),

    /** @see TemperatureFilter */
    TEMPERATURE(TemperatureFilter.class),

    /** @see TintFilter */
    TINT(TintFilter.class),

    LEVEL(ImageLevelFilter.class),
    WHITE_BALANCE(WhiteBalanceFilter.class),

    /** @see VignetteFilter */
    VIGNETTE(VignetteFilter.class);

    private Class<? extends Filter> filterClass;

    Filters(@NonNull Class<? extends Filter> filterClass) {
        this.filterClass = filterClass;
    }

    /**
     * Returns a new instance of the given filter.
     * @return a new instance
     */
    @NonNull
    public Filter newInstance() {
        try {
            return filterClass.newInstance();
        } catch (IllegalAccessException e) {
            return new NoFilter();
        } catch (InstantiationException e) {
            return new NoFilter();
        }
    }
}
