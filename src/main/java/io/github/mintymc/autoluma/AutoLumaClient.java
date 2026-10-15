package io.github.mintymc.autoluma;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

/** Local controller: sunlight detection -> smooth intensity -> render backend. */
public final class AutoLumaClient implements ClientModInitializer {
    private static AutoLumaConfig config;
    private static float intensity;
    private static int effectiveLight = 15;
    private static int blockLight = 15;
    private static int skyLight = 15;
    private static int skyDarken = 0;
    private static boolean irisActive;
    private static boolean vitrailShaderActive;
    private static volatile int gammaHookCalls;
    private static volatile int nightVisionHookCalls;
    private static volatile int postRequestCalls;

    @Override
    public void onInitializeClient() {
        config = AutoLumaConfig.load();
        // Prevent a previously saved Vulkan post-effect from being selected
        // on a renderer which reported native pipeline-creation crashes.
        if (VitrailCompat.LOADED
                && config.renderMode == AutoLumaConfig.RenderMode.POST_EFFECT) {
            config.renderMode = AutoLumaConfig.RenderMode.NIGHT_VISION;
        }
        ClientTickEvents.END_CLIENT_TICK.register(AutoLumaClient::tick);
    }

    public static AutoLumaConfig config() { return config; }
    public static float intensity() { return intensity; }
    public static int sampledLight() { return effectiveLight; }
    public static int sampledBlock() { return blockLight; }
    public static int sampledSky() { return skyLight; }
    public static int sampledSkyDarken() { return skyDarken; }
    public static boolean shadersActive() { return irisActive || vitrailShaderActive; }
    public static boolean postEffectsBlocked() { return VitrailCompat.LOADED; }
    public static String shaderProvider() {
        if (VitrailCompat.LOADED) return vitrailShaderActive ? "Vitrail" : "Vitrail (off)";
        if (irisActive) return "Iris";
        return "Vanilla";
    }
    public static boolean isDark() { return effectiveLight <= config.darkThreshold; }
    public static int gammaHookCalls() { return gammaHookCalls; }
    public static int nightVisionHookCalls() { return nightVisionHookCalls; }
    public static int postRequestCalls() { return postRequestCalls; }
    public static void markGammaHook() { gammaHookCalls++; }
    public static void markNightVisionHook() { nightVisionHookCalls++; }
    public static void markPostRequest() { postRequestCalls++; }

    public static AutoLumaConfig.RenderMode effectiveMode() {
        if (config == null) return AutoLumaConfig.RenderMode.NIGHT_VISION;
        return AutoLumaRenderPolicy.select(config.renderMode, irisActive, VitrailCompat.LOADED);
    }

    public static float bonusGamma() {
        return effectiveMode() == AutoLumaConfig.RenderMode.GAMMA
                ? AutoLumaLight.gammaBonus(intensity) : 0.0f;
    }

    public static float nightVisionStrength() {
        return effectiveMode() == AutoLumaConfig.RenderMode.NIGHT_VISION
                ? AutoLumaLight.nightVisionStrength(intensity) : 0.0f;
    }

    /**
     * Extra support for shader packs using screenBrightness instead of
     * nightVision. Never alters Minecraft's saved gamma preference.
     */
    public static float shaderScreenBrightness(float vanilla) {
        if (config == null) return vanilla;
        AutoLumaConfig.RenderMode mode = effectiveMode();
        if (mode == AutoLumaConfig.RenderMode.NIGHT_VISION
                || mode == AutoLumaConfig.RenderMode.GAMMA) {
            return AutoLumaLight.shaderScreenBrightness(vanilla, intensity);
        }
        return vanilla;
    }

    public static int postLevel() {
        return effectiveMode() == AutoLumaConfig.RenderMode.POST_EFFECT
                ? AutoLumaLight.postLevel(intensity) : 0;
    }

    /**
     * Called by GUI when settings change; also runs while the world is paused.
     * This avoids the old bug where dragging a slider had no immediate effect.
     */
    public static void previewSettings() {
        config.clamp();
        Minecraft client = Minecraft.getInstance();
        sampleLight(client);
        intensity = targetIntensity();
    }

    private static void tick(Minecraft client) {
        if (config == null) return;
        if (client.level == null || client.player == null) {
            intensity = 0.0f;
            effectiveLight = 15;
            blockLight = 15;
            skyLight = 15;
            skyDarken = 0;
            irisActive = false;
            vitrailShaderActive = false;
            return;
        }
        sampleLight(client);
        intensity = AutoLumaLight.smooth(intensity, targetIntensity(), config.transitionTicks);
    }

    private static void sampleLight(Minecraft client) {
        if (client.level == null || client.player == null) return;
        BlockPos feet = client.player.blockPosition();
        BlockPos eyes = BlockPos.containing(client.player.getEyePosition());
        blockLight = Math.max(client.level.getBrightness(LightLayer.BLOCK, feet),
                              client.level.getBrightness(LightLayer.BLOCK, eyes));
        skyLight = Math.max(client.level.getBrightness(LightLayer.SKY, feet),
                            client.level.getBrightness(LightLayer.SKY, eyes));
        skyDarken = client.level.getSkyDarken();
        effectiveLight = AutoLumaLight.effectiveLight(blockLight, skyLight, skyDarken);
        irisActive = IrisCompat.isActive();
        vitrailShaderActive = VitrailCompat.shaderPackActive();
    }

    private static float targetIntensity() {
        return AutoLumaLight.strength(
            AutoLumaLight.darknessStrength(effectiveLight, config.darkThreshold),
            config.strengthPercent, config.enabled, config.forceBoost);
    }
}
