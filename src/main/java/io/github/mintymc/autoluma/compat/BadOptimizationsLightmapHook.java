package io.github.mintymc.autoluma.compat;

import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.Minecraft;

import java.util.function.BooleanSupplier;

/**
 * BadOptimizations 26.3 optional cache hook.
 *
 * Registered with fabric.mod.json's "badoptimizations:cache_hooks" metadata.
 * BadOptimizations discovers and constructs this class reflectively; it does
 * not require a hard dependency or direct reference to BadOptimizations.
 *
 * It is invoked from BadOptimizations' LightmapRenderStateExtractor tick,
 * so a false result allows all its existing caching optimizations to work.
 */
public final class BadOptimizationsLightmapHook implements BooleanSupplier {
    private final LightmapCacheChangeTracker tracker = new LightmapCacheChangeTracker();

    // Required by BadOptimizations HookCreator: public no-argument constructor.
    public BadOptimizationsLightmapHook() {}

    @Override
    public boolean getAsBoolean() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return tracker.shouldUpdate(null, 0.0f, 0.0f);
        }

        return tracker.shouldUpdate(
                minecraft.level,
                AutoLumaClient.bonusGamma(),
                AutoLumaClient.nightVisionStrength());
    }
}
