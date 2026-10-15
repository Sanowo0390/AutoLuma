package io.github.mintymc.autoluma;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** User settings; this is separate from the vanilla brightness option. */
public final class AutoLumaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autoluma.json");

    public enum RenderMode {
        AUTO, POST_EFFECT, NIGHT_VISION, GAMMA
    }

    public boolean enabled = true;
    public boolean forceBoost = false;
    public int strengthPercent = 70;
    public int darkThreshold = 11;
    public int transitionTicks = 8;
    public RenderMode renderMode = RenderMode.AUTO;

    public static AutoLumaConfig load() {
        if (Files.isRegularFile(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                AutoLumaConfig config = GSON.fromJson(json, AutoLumaConfig.class);
                if (config != null) {
                    // v1.0.x: "boostPercent" represented gamma * 100, not luminance.
                    // Preserve the relative slider position on upgrade.
                    if (!json.has("strengthPercent") && json.has("boostPercent")) {
                        config.strengthPercent = Math.round(json.get("boostPercent").getAsInt() / 15.0f);
                    }
                    // Preserve a user's explicit legacy night-vision preference.
                    if (!json.has("renderMode") && json.has("useNightVision")
                            && json.get("useNightVision").getAsBoolean()) {
                        config.renderMode = RenderMode.NIGHT_VISION;
                    }
                    config.clamp();
                    return config;
                }
            } catch (Exception e) {
                System.err.println("[AutoLuma] Could not load settings: " + e);
            }
        }
        return new AutoLumaConfig();
    }

    public void save() {
        clamp();
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[AutoLuma] Could not save settings: " + e);
        }
    }

    public void clamp() {
        strengthPercent = Math.max(0, Math.min(100, strengthPercent));
        darkThreshold = Math.max(0, Math.min(14, darkThreshold));
        transitionTicks = Math.max(1, Math.min(60, transitionTicks));
        if (renderMode == null) renderMode = RenderMode.AUTO;
    }
}
