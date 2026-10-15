package io.github.mintymc.autoluma;

import java.util.function.DoubleSupplier;

/** Pure lighting, smoothing and quantization helpers; covered by unit tests. */
public final class AutoLumaLight {
    private AutoLumaLight() {}

    public static int effectiveLight(int blockLight, int skyLight, int skyDarken) {
        return Math.max(clamp(blockLight, 0, 15),
                Math.max(0, clamp(skyLight, 0, 15) - clamp(skyDarken, 0, 15)));
    }

    /** 0 = bright, 1 = pitch-black. Continuous, rather than an on/off gate. */
    public static float darknessStrength(int effectiveLight, int darkThreshold) {
        int threshold = clamp(darkThreshold, 0, 14);
        return clamp01((threshold + 1.0f - clamp(effectiveLight, 0, 15)) / (threshold + 1.0f));
    }

    /** UI controls intensity, not a physically linear display luminance multiplier. */
    public static float strength(float darknessStrength, int strengthPercent, boolean enabled, boolean forceBoost) {
        if (!enabled) return 0.0f;
        return clamp01((forceBoost ? 1.0f : darknessStrength) *
                clamp(strengthPercent, 0, 100) / 100.0f);
    }

    public static float smooth(float previous, float target, int ticks) {
        float current = previous + (target - previous) / Math.max(1, ticks);
        return Math.abs(current - target) < 0.0005f ? target : current;
    }

    /** 12 discrete full-frame post-effect levels, balancing smoothness and cache size. */
    public static int postLevel(float intensity) {
        return clamp(Math.round(clamp01(intensity) * 12.0f), 0, 12);
    }

    public static float gammaBonus(float intensity) {
        return 15.0f * clamp01(intensity);
    }

    public static float nightVisionStrength(float intensity) {
        return clamp01(intensity);
    }

    /**
     * Vanilla's nightVisionScale requires a non-null MobEffectInstance.
     * The lightmap may invoke it even without that instance because AutoLuma
     * enables its visual branch. Never call original in that situation.
     */
    public static float resolveNightVisionStrength(boolean hasRealNightVision,
                                                    float localStrength,
                                                    DoubleSupplier originalStrength) {
        float local = nightVisionStrength(localStrength);
        if (!hasRealNightVision) {
            return local;
        }
        return Math.max(local, (float) originalStrength.getAsDouble());
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
