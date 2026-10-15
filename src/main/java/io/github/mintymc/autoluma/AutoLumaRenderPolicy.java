package io.github.mintymc.autoluma;

/**
 * Choose a conservative shader-aware backend.
 * Iris and Vitrail both expose a nightVision uniform. Enabling a Minecraft
 * custom post effect instead was the cause of invisible correction on Iris
 * and coincided with a Vulkan pipeline-creation crash on Vitrail.
 *
 * Keep manual post effects available only outside Vitrail. Auto never
 * selects them until full-screen runtime rendering is tested in-game.
 */
public final class AutoLumaRenderPolicy {
    private AutoLumaRenderPolicy() {}

    public static AutoLumaConfig.RenderMode select(AutoLumaConfig.RenderMode requested,
                                                    boolean irisShadersActive,
                                                    boolean vitrailLoaded) {
        if (requested == null || requested == AutoLumaConfig.RenderMode.AUTO) {
            return AutoLumaConfig.RenderMode.NIGHT_VISION;
        }
        if (vitrailLoaded && requested == AutoLumaConfig.RenderMode.POST_EFFECT) {
            return AutoLumaConfig.RenderMode.NIGHT_VISION;
        }
        return requested;
    }
}
