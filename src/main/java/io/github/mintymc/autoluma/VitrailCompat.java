package io.github.mintymc.autoluma;

import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Compatibility with Vitrail's optional PackChoice.session() accessor,
 * which reports a loaded shader pack. A reflection-only check means AutoLuma
 * does not depend on Vitrail at compile time or when using Iris.
 */
public final class VitrailCompat {
    public static final boolean LOADED = FabricLoader.getInstance().isModLoaded("vitrail");
    private static final Method SESSION;
    private static final Method NO_PACK_WANTED;

    static {
        Method session = null;
        Method noPackWanted = null;
        if (LOADED) {
            try {
                Class<?> type = Class.forName("dev.vitrail.render.PackChoice");
                session = type.getMethod("session");
                noPackWanted = type.getMethod("noPackWanted");
            } catch (ReflectiveOperationException | LinkageError e) {
                System.err.println("[AutoLuma] Vitrail pack state unavailable: " + e);
            }
        }
        SESSION = session;
        NO_PACK_WANTED = noPackWanted;
    }

    private VitrailCompat() {}

    public static boolean shaderPackActive() {
        if (!LOADED || SESSION == null || NO_PACK_WANTED == null) return false;
        try {
            Object off = NO_PACK_WANTED.invoke(null);
            Object loaded = SESSION.invoke(null);
            return !Boolean.TRUE.equals(off)
                    && loaded instanceof Optional<?> optional
                    && optional.isPresent();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return false;
        }
    }
}
