package io.github.mintymc.autoluma.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.mintymc.autoluma.AutoLumaClient;
import io.github.mintymc.autoluma.AutoLumaLight;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * LightmapRenderStateExtractor allows AutoLuma night vision even without a
 * real potion effect. In that case vanilla GameRenderer.nightVisionScale()
 * dereferences a missing MobEffectInstance and crashes.
 *
 * Only invoke the original computation when the actual effect exists.
 * Never add fake potion effects or alter the server-side entity.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererNightVisionMixin {
    @WrapMethod(method = "nightVisionScale")
    private static float autoluma$nightVisionScale(LivingEntity entity, float tickProgress,
                                                   Operation<Float> original) {
        AutoLumaClient.markNightVisionHook();

        // hasEffect() may be patched by other mods. The actual instance must
        // exist because vanilla nightVisionScale() calls endsWithin() on it.
        boolean hasRealNightVision = entity.getEffect(MobEffects.NIGHT_VISION) != null;
        return AutoLumaLight.resolveNightVisionStrength(
                hasRealNightVision,
                AutoLumaClient.nightVisionStrength(),
                () -> original.call(entity, tickProgress));
    }
}
