package io.github.mintymc.autoluma.compat;

/**
 * Tracks the two quantities AutoLuma adds to Minecraft's lightmap.
 *
 * BadOptimizations already detects vanilla gamma and genuine potion effects,
 * but not AutoLuma's client-only gamma/night-vision strengths. The cache
 * should be invalidated when (and only when) those strengths or the world
 * identity change. Tracking values instead of just "effect is active"
 * correctly covers fade-in, fade-out and changes between render modes.
 *
 * No dependency on Minecraft or BadOptimizations: unit-testable alone.
 */
public final class LightmapCacheChangeTracker {
    private Object previousWorld;
    private int previousGammaBits;
    private int previousNightVisionBits;

    /**
     * @return true when the lightmap needs to be recalculated.
     * A null world clears the snapshot so rejoining always refreshes.
     */
    public boolean shouldUpdate(Object world, float gammaBonus, float nightVisionStrength) {
        if (world == null) {
            previousWorld = null;
            previousGammaBits = 0;
            previousNightVisionBits = 0;
            return false;
        }

        int gammaBits = Float.floatToIntBits(gammaBonus);
        int nightVisionBits = Float.floatToIntBits(nightVisionStrength);
        boolean dirty = previousWorld != world
                || gammaBits != previousGammaBits
                || nightVisionBits != previousNightVisionBits;

        previousWorld = world;
        previousGammaBits = gammaBits;
        previousNightVisionBits = nightVisionBits;
        return dirty;
    }
}
