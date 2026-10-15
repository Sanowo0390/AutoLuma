package io.github.mintymc.autoluma;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Protect the connection between the twelve generated resource-pack effects
 * and the Java-side postLevel selection. Does not replace game GPU testing.
 */
class PostEffectsAssetTest {
    @Test
    void everyPostEffectHasValidShaderAndTarget() throws Exception {
        Path base = Path.of("src/main/resources/assets/autoluma");
        assertTrue(Files.isRegularFile(base.resolve("shaders/post/brightness_lift.fsh")));
        Set<Float> seen = new HashSet<>();
        for (int level = 1; level <= 12; level++) {
            Path file = base.resolve("post_effect/lift_" + level + ".json");
            assertTrue(Files.isRegularFile(file), "Missing " + file);
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            assertEquals(2, root.getAsJsonArray("passes").size());
            JsonObject first = root.getAsJsonArray("passes").get(0).getAsJsonObject();
            assertEquals("autoluma:post/brightness_lift", first.get("fragment_shader").getAsString());
            assertEquals("minecraft:main",
                    first.getAsJsonArray("inputs").get(0).getAsJsonObject().get("target").getAsString());
            assertEquals("swap", first.get("output").getAsString());
            float value = first.getAsJsonObject("uniforms").getAsJsonArray("LiftConfig")
                    .get(0).getAsJsonObject().get("value").getAsFloat();
            assertEquals(level / 12.0f, value, 0.00001f);
            seen.add(value);
            JsonObject second = root.getAsJsonArray("passes").get(1).getAsJsonObject();
            assertEquals("minecraft:main", second.get("output").getAsString());
        }
        assertEquals(12, seen.size());
    }
}
