package me.chubbyduck.canvas.layout;

import java.util.List;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/**
 * Given to {@link me.chubbyduck.canvas.node.Node#measure} and {@link me.chubbyduck.canvas.node.Node#arrange}.
 */
@NullMarked
public interface LayoutContext {

    Theme theme();

    /**
     * The widest this node may be.
     */
    int maxWidth();

    List<Child> children();

    /**
     * Width of a single line of styled text in the default font.
     */
    int textWidth(Component text);

    Size spriteSize(Sprite sprite);

    /**
     * A resolved child.
     */
    @NullMarked
    interface Child {

        Node node();

        Size measure();

        Size measure(int maxWidth);

        /**
         * Assigns the child's rectangle.
         */
        void place(Rect rect);

    }
}
