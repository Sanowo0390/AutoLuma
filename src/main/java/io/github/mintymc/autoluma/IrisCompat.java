package io.github.mintymc.autoluma;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/**
 * Uses Iris's documented public API through reflection; no hard Iris
 * dependency, and no access to unstable Iris internal rendering classes.
 */
public final class IrisCompat {
    private static final Object instance;
    private static final Method isShaderPackInUse;

    static {
        Object api = null;
        Method checker = null;
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
                Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = type.getMethod("getInstance").invoke(null);
                checker = type.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError e) {
                System.err.println("[AutoLuma] Iris detection unavailable: " + e);
            }
        }
        instance = api;
        isShaderPackInUse = checker;
    }

    private IrisCompat() {}

    public static boolean isActive() {
        if (instance == null || isShaderPackInUse == null) return false;
        try {
            return Boolean.TRUE.equals(isShaderPackInUse.invoke(instance));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
