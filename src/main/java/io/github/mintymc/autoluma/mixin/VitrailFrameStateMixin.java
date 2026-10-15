package io.github.mintymc.autoluma.mixin;

import io.github.mintymc.autoluma.AutoLumaClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vitrail does not reuse Minecraft's extracted lightmap for shader-pack
 * uniforms. It computes FrameState.nightVision() from real potion effects.
 *
 * Supply AutoLuma's visual-only night-vision strength directly to the
 * uniform getter without adding fake potion effects or creating new Vulkan
 * pipelines. This optional mixin is inert when Vitrail is not installed.
 *
 * Shader packs may ignore the uniform; this is not universal compatibility.
 */
@Pseudo
@Mixin(targets = "dev.vitrail.render.FrameState", remap = false)
public abstract class VitrailFrameStateMixin {
    @Inject(method = "nightVision()F", at = @At("RETURN"),
            cancellable = true, remap = false, require = 0)
    private void autoluma$nightVisionUniform(CallbackInfoReturnable<Float> info) {
        AutoLumaClient.markVitrailUniformRead();
        float boost = AutoLumaClient.nightVisionStrength();
        if (boost > 0.0f) {
            info.setReturnValue(Math.max(info.getReturnValueF(), boost));
        }
    }

    /**
     * Shader packs may use screenBrightness instead of nightVision.
     * Pass the same smooth strength to both uniforms in Auto mode.
     * This never overwrites the user's saved gamma option.
     */
    @Inject(method = "screenBrightness()F", at = @At("RETURN"),
            cancellable = true, remap = false, require = 0)
    private void autoluma$brightnessUniform(CallbackInfoReturnable<Float> info) {
        AutoLumaClient.markVitrailUniformRead();
        float vanilla = info.getReturnValueF();
        float adjusted = AutoLumaClient.shaderScreenBrightness(vanilla);
        if (adjusted > vanilla) info.setReturnValue(adjusted);
    }
}
