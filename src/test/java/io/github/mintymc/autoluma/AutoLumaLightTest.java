package io.github.mintymc.autoluma;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoLumaLightTest {
    @Test
    void renderModeSelectionGuardsVitrailPostProcessing() {
        assertEquals(AutoLumaConfig.RenderMode.NIGHT_VISION,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.POST_EFFECT, false, true));
        assertEquals(AutoLumaConfig.RenderMode.NIGHT_VISION,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.AUTO, true, true));
        assertEquals(AutoLumaConfig.RenderMode.NIGHT_VISION,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.AUTO, false, false));
        assertEquals(AutoLumaConfig.RenderMode.NIGHT_VISION,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.AUTO, true, false));
        assertEquals(AutoLumaConfig.RenderMode.POST_EFFECT,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.POST_EFFECT, false, false));
        assertEquals(AutoLumaConfig.RenderMode.GAMMA,
            AutoLumaRenderPolicy.select(AutoLumaConfig.RenderMode.GAMMA, false, true));
    }

    @Test
    void shaderBrightnessRespectsBoundsAndPreservesVanilla() {
        assertEquals(0.6f, AutoLumaLight.shaderScreenBrightness(0.6f, 0.0f), 0.00001f);
        assertEquals(0.8f, AutoLumaLight.shaderScreenBrightness(0.6f, 0.5f), 0.00001f);
        assertEquals(1.0f, AutoLumaLight.shaderScreenBrightness(0.6f, 1.0f), 0.00001f);
        assertEquals(1.0f, AutoLumaLight.shaderScreenBrightness(1.0f, 0.9f), 0.00001f);
        assertEquals(0.0f, AutoLumaLight.shaderScreenBrightness(-1.0f, 0.0f), 0.00001f);
    }

    @Test
    void transitionTicksAccept100() {
        assertEquals(100, AutoLumaLight.clampTransitionTicks(100));
        assertEquals(100, AutoLumaLight.clampTransitionTicks(101));
        assertEquals(1, AutoLumaLight.clampTransitionTicks(0));
        assertEquals(0.01f, AutoLumaLight.smooth(0.0f, 1.0f, 100), 0.00001f);
    }

    @Test
    void fullDaylightNoAdjustment() {
        assertEquals(15, AutoLumaLight.effectiveLight(0, 15, 0));
        assertEquals(0.0f, AutoLumaLight.darknessStrength(15, 11));
    }

    @Test
    void unlitCaveFullAdjustment() {
        assertEquals(0, AutoLumaLight.effectiveLight(0, 0, 0));
        assertEquals(1.0f, AutoLumaLight.darknessStrength(0, 11));
    }

    @Test
    void nightSkyAccountsForAmbientDarkening() {
        assertEquals(4, AutoLumaLight.effectiveLight(0, 15, 11));
        assertTrue(AutoLumaLight.darknessStrength(4, 11) > 0.5f);
    }

    @Test
    void blockLightCanOffsetDarkNight() {
        assertEquals(14, AutoLumaLight.effectiveLight(14, 15, 14));
        assertEquals(0.0f, AutoLumaLight.darknessStrength(14, 11));
    }

    @Test
    void lowerLightMeansStrongerAdjustment() {
        float previously = 0.0f;
        for (int level = 15; level >= 0; --level) {
            float now = AutoLumaLight.darknessStrength(level, 11);
            assertTrue(now >= previously, "strength must grow in darker areas");
            previously = now;
        }
    }

    @Test
    void configurableThresholdAndForceMode() {
        assertEquals(1.0f, AutoLumaLight.darknessStrength(0, 0));
        assertEquals(0.0f, AutoLumaLight.darknessStrength(1, 0));
        assertEquals(0.0f, AutoLumaLight.strength(1.0f, 100, false, true));
        assertEquals(0.3f, AutoLumaLight.strength(1.0f, 30, true, true), 0.00001f);
        assertEquals(0.0f, AutoLumaLight.strength(0.0f, 100, true, false));
    }

    @Test
    void smoothingGraduallyReturnsToZero() {
        float v = 1.0f;
        for (int i = 0; i < 130; i++) {
            float next = AutoLumaLight.smooth(v, 0.0f, 8);
            assertTrue(next <= v);
            v = next;
        }
        assertEquals(0.0f, v, 0.0006f);
        assertEquals(0.5f, AutoLumaLight.smooth(0.0f, 1.0f, 2));
        assertEquals(1.0f, AutoLumaLight.smooth(0.0f, 1.0f, 1));
    }

    @Test
    void postLevelsHaveValidBounds() {
        assertEquals(0, AutoLumaLight.postLevel(0.0f));
        assertEquals(6, AutoLumaLight.postLevel(0.5f));
        assertEquals(12, AutoLumaLight.postLevel(1.0f));
        assertEquals(12, AutoLumaLight.postLevel(9.0f));
        assertEquals(0, AutoLumaLight.postLevel(-9.0f));
    }

    @Test
    void missingRealNightVisionDoesNotCallVanillaImplementation() {
        // Regression: Minecraft 26.3 GameRenderer.nightVisionScale throws an
        // NPE on missing MobEffectInstance; the supplier MUST remain untouched.
        assertEquals(0.7f, AutoLumaLight.resolveNightVisionStrength(
            false, 0.7f, () -> { throw new NullPointerException("nightVision"); }));
        assertEquals(0.0f, AutoLumaLight.resolveNightVisionStrength(
            false, 0.0f, () -> { throw new NullPointerException("nightVision"); }));
    }

    @Test
    void realNightVisionStillRespectsVanillaStrength() {
        assertEquals(0.9f, AutoLumaLight.resolveNightVisionStrength(
            true, 0.5f, () -> 0.9), 0.00001f);
        assertEquals(0.8f, AutoLumaLight.resolveNightVisionStrength(
            true, 0.8f, () -> 0.4), 0.00001f);
    }

    @Test
    void fallbackModesHaveSaneStrengths() {
        assertEquals(0.0f, AutoLumaLight.gammaBonus(0.0f));
        assertEquals(15.0f, AutoLumaLight.gammaBonus(1.0f));
        assertEquals(0.25f, AutoLumaLight.nightVisionStrength(0.25f));
    }
}
