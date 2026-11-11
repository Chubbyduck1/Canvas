package me.chubbyduck.canvas.node;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Bridge the Canvas runtime uses to call {@link Node}'s protected lifecycle methods.
 */
@NullMarked
@ApiStatus.Internal
public final class NodeInternals {

    private static final Map<Class<?>, Boolean> OVERRIDES_REGISTER = new ConcurrentHashMap<>();

    private NodeInternals() {
    }

    public static void register(final Node node, final AssetRegistrar assets) {
        node.register(assets);
    }

    public static Size measure(final Node node, final LayoutContext context) {
        return node.measure(context);
    }

    public static void arrange(final Node node, final Rect rect, final LayoutContext context) {
        node.arrange(rect, context);
    }

    public static void paint(final Node node, final PaintContext context) {
        node.paint(context);
    }

    public static void plain(final Node node, final PlainContext context) {
        node.plain(context);
    }

    /**
     * Whether the class only inherits {@link Node}'s empty {@code register}, so it has no textures to put in the pack.
     */
    public static boolean hasDefaultRegister(final Class<? extends Node> type) {
        return !OVERRIDES_REGISTER.computeIfAbsent(type, unused -> NodeInternals.registerOwner(type) != null);
    }

    /**
     * The class that actually declares {@code register} for the given node class (it may be a superclass), or {@code
     * null} if none does.
     */
    public static @Nullable Class<?> registerOwner(final Class<? extends Node> type) {
        Class<?> current = type;
        while (current != null && current != Node.class) {
            for (final Method method : current.getDeclaredMethods()) {
                if (method.getName().equals("register")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == AssetRegistrar.class) {
                    return current;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * Creates a throwaway instance through the class's no-arg constructor (any visibility).
     */
    public static <N extends Node> N instantiate(final Class<N> type) {
        try {
            final Constructor<N> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final NoSuchMethodException exception) {
            throw new IllegalStateException(
                "Node class " + type.getName() + " overrides register(...) but has no no-arg constructor."
                    + " Add one (it may be private) so Canvas can register its textures.", exception);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not instantiate node class " + type.getName(), exception);
        }
    }
}
