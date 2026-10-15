package io.github.mintymc.autoluma;

/**
 * Defensive renderer selection: Vitrail uses Minecraft's experimental Vulkan
 * backend. AutoLuma's custom screen post-effects are currently unverified
 * there; adding one can lead to a native crash with no Java stack trace.
 * Avoid queueing the post effect when Vitrail is loaded.
 */
public final class AutoLumaRenderPolicy {
    private AutoLumaRenderPolicy() {}

    public static AutoLumaConfig.RenderMode select(AutoLumaConfig.RenderMode requested,
                                                    boolean irisShadersActive,
                                                    boolean vitrailLoaded) {
        if (requested == null || requested == AutoLumaConfig.RenderMode.AUTO) {
            requested = irisShadersActive
                    ? AutoLumaConfig.RenderMode.POST_EFFECT
                    : AutoLumaConfig.RenderMode.NIGHT_VISION;
        }
        if (vitrailLoaded && requested == AutoLumaConfig.RenderMode.POST_EFFECT) {
            return AutoLumaConfig.RenderMode.NIGHT_VISION;
        }
        return requested;
    }
}
