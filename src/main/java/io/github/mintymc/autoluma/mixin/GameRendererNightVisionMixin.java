package io.github.mintymc.autoluma.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds a transient local night-vision lightmap strength for shader packs that
 * ignore vanilla gamma. Does not grant a potion effect or change server state.
 * Custom shader compatibility is not guaranteed.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererNightVisionMixin {
    @WrapMethod(method = "nightVisionScale")
    private static float autoluma$nightVisionScale(LivingEntity entity, float tickProgress,
                                                   Operation<Float> original) {
        float vanillaStrength = original.call(entity, tickProgress);
        return Math.max(vanillaStrength, AutoLumaClient.nightVisionStrength());
    }
}
