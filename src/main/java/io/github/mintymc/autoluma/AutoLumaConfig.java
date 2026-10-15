package io.github.mintymc.autoluma;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AutoLumaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autoluma.json");

    public boolean enabled = true;
    public int boostPercent = 500;
    public int darkThreshold = 7;
    public int transitionTicks = 8;

    public static AutoLumaConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    AutoLumaConfig value = GSON.fromJson(reader, AutoLumaConfig.class);
                    if (value != null) {
                        value.clamp();
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AutoLuma] Could not read settings; using defaults: " + e);
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
        boostPercent = Math.max(0, Math.min(1500, boostPercent));
        darkThreshold = Math.max(0, Math.min(13, darkThreshold));
        transitionTicks = Math.max(1, Math.min(60, transitionTicks));
    }
}
