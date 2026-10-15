package io.github.mintymc.autoluma;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

/**
 * Client-only brightness controller. Does not mutate Minecraft's gamma option.
 */
public final class AutoLumaClient implements ClientModInitializer {
    private static AutoLumaConfig config;
    private static boolean dark;
    private static float currentBonus;
    private static int sampledLight = 15;
    private static int sampledBlock = 15;
    private static int sampledSky = 15;
    private static int sampledSkyDarken;

    @Override
    public void onInitializeClient() {
        config = AutoLumaConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(AutoLumaClient::tick);
    }

    public static AutoLumaConfig config() {
        return config;
    }

    /** Extra gamma to add to the extracted lightmap brightness. */
    public static float bonusGamma() {
        return currentBonus;
    }

    /** Enable vanilla-gamma fallback or a night-vision-style lightmap path. */
    public static boolean useNightVision() {
        return config != null && config.useNightVision && config.enabled;
    }

    /**
     * Extra night-vision strength, from 0 to 1.
     * Preserve existing vanilla night vision when the mod is inactive.
     */
    public static float nightVisionStrength() {
        return useNightVision() ? AutoLumaLight.nightVisionStrength(currentBonus) : 0.0f;
    }

    public static int sampledLight() {
        return sampledLight;
    }

    public static int sampledBlock() {
        return sampledBlock;
    }

    public static int sampledSky() {
        return sampledSky;
    }

    public static int sampledSkyDarken() {
        return sampledSkyDarken;
    }

    public static boolean isDark() {
        return dark;
    }

    private static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            dark = false;
            currentBonus = 0.0f;
            sampledLight = 15;
            sampledSky = 15;
            sampledBlock = 15;
            sampledSkyDarken = 0;
            return;
        }
        if (client.isPaused()) {
            return;
        }

        BlockPos feet = client.player.blockPosition();
        BlockPos eyes = BlockPos.containing(client.player.getEyePosition());

        // Separate SKY and BLOCK sampling makes night-time exposure explicit.
        // Sampling both feet and eyes keeps the mod from overreacting to a dark
        // block just below the head in otherwise bright surroundings.
        sampledBlock = Math.max(
                client.level.getBrightness(LightLayer.BLOCK, feet),
                client.level.getBrightness(LightLayer.BLOCK, eyes));
        sampledSky = Math.max(
                client.level.getBrightness(LightLayer.SKY, feet),
                client.level.getBrightness(LightLayer.SKY, eyes));
        sampledSkyDarken = client.level.getSkyDarken();
        sampledLight = AutoLumaLight.effectiveLight(sampledBlock, sampledSky, sampledSkyDarken);

        dark = AutoLumaLight.isDark(sampledLight, config.darkThreshold, dark);
        float target = (config.enabled && (config.forceBoost || dark))
                ? config.boostPercent / 100.0f : 0.0f;

        // Smoothed transitions; resetting to 0 restores the exact vanilla render
        // value automatically. The user-adjusted brightness slider remains intact.
        currentBonus += (target - currentBonus) / Math.max(1, config.transitionTicks);
        if (Math.abs(target - currentBonus) < 0.001f) {
            currentBonus = target;
        }
    }
}
