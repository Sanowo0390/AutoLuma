package io.github.mintymc.autoluma.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla gamma: apply the transient bonus only to the extracted render state.
 * Optional night-vision mode: keep gamma unchanged and activate the client-side
 * night-vision lightmap branch. Never change options.txt or player effects.
 */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapExtractorMixin {
    @ModifyExpressionValue(
        method = "extract",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0)
    )
    private float autoluma$adjustBrightness(float vanillaBrightness) {
        return AutoLumaClient.useNightVision() ? vanillaBrightness :
                Math.max(0.0F, vanillaBrightness + AutoLumaClient.bonusGamma());
    }

    @ModifyExpressionValue(
        method = "extract",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
                 ordinal = 0)
    )
    private boolean autoluma$enableNightVisionLightmap(boolean vanillaHasNightVision) {
        return vanillaHasNightVision || AutoLumaClient.nightVisionStrength() > 0.001f;
    }
}
