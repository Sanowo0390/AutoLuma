package io.github.mintymc.autoluma.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Client-side visual fallback; preserves any actual night-vision effect. */
@Mixin(GameRenderer.class)
public abstract class GameRendererNightVisionMixin {
    @WrapMethod(method = "nightVisionScale")
    private static float autoluma$nightVisionScale(LivingEntity entity, float tickProgress,
                                                   Operation<Float> original) {
        AutoLumaClient.markNightVisionHook();
        return Math.max(original.call(entity, tickProgress), AutoLumaClient.nightVisionStrength());
    }
}
