package io.github.mintymc.autoluma;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoLumaLightTest {
    @Test
    void nightVisionStrengthTracksPercentSlider() {
        assertEquals(0.0f, AutoLumaLight.nightVisionStrength(0.0f));
        assertEquals(1.0f / 3.0f, AutoLumaLight.nightVisionStrength(5.0f), 0.00001f);
        assertEquals(1.0f, AutoLumaLight.nightVisionStrength(15.0f));
        assertEquals(1.0f, AutoLumaLight.nightVisionStrength(20.0f));
    }

    @Test
    void nightSkyIsNotTreatedAsFullDaylight() {
        assertEquals(4, AutoLumaLight.effectiveLight(0, 15, 11));
    }

    @Test
    void exposedDaylightIsBright() {
        assertEquals(15, AutoLumaLight.effectiveLight(0, 15, 0));
    }

    @Test
    void darkCaveActivates() {
        assertEquals(0, AutoLumaLight.effectiveLight(0, 0, 0));
        assertTrue(AutoLumaLight.isDark(0, 11, false));
    }

    @Test
    void torchLightCanPreventActivation() {
        assertEquals(14, AutoLumaLight.effectiveLight(14, 0, 0));
        assertFalse(AutoLumaLight.isDark(14, 11, false));
    }

    @Test
    void hysteresisPreventsFlickerAtThreshold() {
        assertTrue(AutoLumaLight.isDark(11, 11, false));
        assertTrue(AutoLumaLight.isDark(12, 11, true));
        assertFalse(AutoLumaLight.isDark(13, 11, true));
    }

    @Test
    void skyCannotCancelNearbyBlockLight() {
        assertEquals(9, AutoLumaLight.effectiveLight(9, 15, 14));
    }
}
