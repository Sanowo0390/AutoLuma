package io.github.mintymc.autoluma.mixin;

import io.github.mintymc.autoluma.AutoLumaClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.DoubleSupplier;

/**
 * Optional integration for Iris 26.3.
 *
 * Iris computes its nightVision uniform in CommonUniforms#getNightVision.
 * It uses the GameRenderer scale even when no potion effect is present, but
 * patching the uniform result ensures AutoLuma reaches the shader pipeline.
 *
 * Iris screenBrightness comes from a separate lambda registered as a
 * DoubleSupplier, not the vanilla LightmapRenderStateExtractor; inject the
 * per-frame supplier while retaining the player's original gamma setting.
 *
 * No compile-time Iris dependency. If Iris changes either private symbol,
 * optional require=0 prevents a game-startup failure.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public abstract class IrisUniformsMixin {
    @Inject(method = "getNightVision()F", at = @At("RETURN"),
            cancellable = true, remap = false, require = 0)
    private static void autoluma$nightVisionUniform(CallbackInfoReturnable<Float> info) {
        AutoLumaClient.markIrisUniformRead();
        float boost = AutoLumaClient.nightVisionStrength();
        if (boost > 0.0f) info.setReturnValue(Math.max(info.getReturnValueF(), boost));
    }

    @ModifyArg(method = "generalCommonUniforms", at = @At(value = "INVOKE",
            target = "Lnet/irisshaders/iris/gl/uniform/UniformHolder;uniform1f(Lnet/irisshaders/iris/gl/uniform/UniformUpdateFrequency;Ljava/lang/String;Ljava/util/function/DoubleSupplier;)Lnet/irisshaders/iris/gl/uniform/UniformHolder;",
            ordinal = 0, remap = false),
            index = 2, remap = false, require = 0)
    private static DoubleSupplier autoluma$screenBrightnessUniform(DoubleSupplier original) {
        return () -> {
            AutoLumaClient.markIrisUniformRead();
            return AutoLumaClient.shaderScreenBrightness((float) original.getAsDouble());
        };
    }
}
