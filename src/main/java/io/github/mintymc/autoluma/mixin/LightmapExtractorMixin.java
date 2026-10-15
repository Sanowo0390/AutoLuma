package io.github.mintymc.autoluma.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Minecraft 26.3's extract method sets:
 * renderState.brightness = Math.max(0.0F, brightnessOption - darknessEffectBrightnessModifier)
 *
 * This modifies the extracted lightmap value, not the persistent vanilla gamma option.
 */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapExtractorMixin {
    @ModifyExpressionValue(
        method = "extract",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F")
    )
    private float autoluma$adjustBrightness(float vanillaBrightness) {
        return Math.max(0.0F, vanillaBrightness + AutoLumaClient.bonusGamma());
    }
}
