package me.chubbyduck.canvas.paper.platform;

import java.lang.reflect.Method;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Detects Bedrock players through Floodgate, if it is installed (no compile-time dependency).
 */
@NullMarked
final class BedrockDetector {

    private static final @Nullable Object API;
    private static final @Nullable Method IS_FLOODGATE_PLAYER;

    static {
        Object api = null;
        Method method = null;
        try {
            final Class<?> type = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            api = type.getMethod("getInstance").invoke(null);
            method = type.getMethod("isFloodgatePlayer", UUID.class);
        } catch (final ReflectiveOperationException | LinkageError ignored) {
            // Floodgate isn't installed.
        }
        API = api;
        IS_FLOODGATE_PLAYER = method;
    }

    private BedrockDetector() {
    }

    static boolean isBedrock(final UUID player) {
        if (API == null || IS_FLOODGATE_PLAYER == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(IS_FLOODGATE_PLAYER.invoke(API, player));
        } catch (final ReflectiveOperationException exception) {
            return false;
        }
    }
}
