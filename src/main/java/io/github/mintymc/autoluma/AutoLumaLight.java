package io.github.mintymc.autoluma;

/**
 * Pure light-level and activation calculations. Vanilla's unattenuated sky-light
 * value is 15 outside even at night, so subtract the current sky darkening.
 * Keep light detection separate from the renderer's gamma adjustment.
 */
public final class AutoLumaLight {
    private AutoLumaLight() {}

    public static int effectiveLight(int blockLight, int skyLight, int skyDarken) {
        return Math.max(clamp(blockLight, 0, 15),
                Math.max(0, clamp(skyLight, 0, 15) - clamp(skyDarken, 0, 15)));
    }

    public static boolean isDark(int effectiveLight, int threshold, boolean previouslyDark) {
        int enter = clamp(threshold, 0, 14);
        if (previouslyDark) {
            // Separate exit threshold avoids flicker around lights or doorways.
            return effectiveLight < Math.min(15, enter + 2);
        }
        return effectiveLight <= enter;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
