package io.github.mintymc.autoluma.mixin;

import io.github.mintymc.autoluma.AutoLumaClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft 26.3 GameRenderer collects LocalPlayer#getActivePostEffects every
 * frame, then applies them after world rendering and before HUD rendering.
 * Return a new list so server-managed effects are never mutated or removed.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerPostEffectsMixin {
    @Inject(method = "getActivePostEffects", at = @At("RETURN"), cancellable = true)
    private void autoluma$includePostEffect(CallbackInfoReturnable<List<Identifier>> info) {
        int level = AutoLumaClient.postLevel();
        if (level <= 0) return;
        Identifier id = Identifier.fromNamespaceAndPath("autoluma", "lift_" + level);
        List<Identifier> effects = new ArrayList<>(info.getReturnValue());
        if (!effects.contains(id)) {
            effects.add(id);
        }
        AutoLumaClient.markPostRequest();
        info.setReturnValue(effects);
    }
}
