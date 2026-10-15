package io.github.mintymc.autoluma;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Client-only tick logic; never modifies the player's saved vanilla gamma option.
 * Rendering gets an extra gamma value through LightmapExtractorMixin.
 */
public final class AutoLumaClient implements ClientModInitializer {
    private static AutoLumaConfig config;
    private static boolean dark;
    private static float currentBonus;
    private static int sampledLight = 15;

    @Override
    public void onInitializeClient() {
        config = AutoLumaConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(AutoLumaClient::tick);
    }

    public static AutoLumaConfig config() {
        return config;
    }

    public static float bonusGamma() {
        return currentBonus;
    }

    public static int sampledLight() {
        return sampledLight;
    }

    private static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            dark = false;
            currentBonus = 0.0f;
            sampledLight = 15;
            return;
        }
        if (client.isPaused()) {
            return;
        }

        BlockPos feet = client.player.blockPosition();
        BlockPos eyes = BlockPos.containing(client.player.getEyePosition());
        sampledLight = Math.max(client.level.getMaxLocalRawBrightness(feet),
                                client.level.getMaxLocalRawBrightness(eyes));

        int threshold = config.darkThreshold;
        if (!config.enabled) {
            dark = false;
        } else if (dark) {
            // Hysteresis suppresses rapid switching around the threshold.
            if (sampledLight >= Math.min(15, threshold + 2)) {
                dark = false;
            }
        } else if (sampledLight <= threshold) {
            dark = true;
        }

        float target = dark && config.enabled ? config.boostPercent / 100.0f : 0.0f;
        int ticks = Math.max(1, config.transitionTicks);
        currentBonus += (target - currentBonus) / ticks;
        if (Math.abs(target - currentBonus) < 0.001f) {
            currentBonus = target;
        }
    }
}
