package me.chubbyduck.canvas.elements.input;

import java.util.ArrayList;
import java.util.List;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.DialogAction;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Native footer buttons ("Reset", "Done"...). */
@NullMarked
public class ActionBar extends Node {

    private final List<DialogAction> actions;

    private ActionBar(final List<DialogAction> actions) {
        this.actions = List.copyOf(actions);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void paint(final PaintContext context) {
        for (final DialogAction action : this.actions) {
            context.footer(action);
        }
    }

    @Override
    protected void plain(final PlainContext context) {
        for (final DialogAction action : this.actions) {
            context.footer(action);
        }
    }

    public static class Builder {

        private final List<DialogAction> actions = new ArrayList<>();

        public Builder button(final String label, final Runnable onClick) {
            return this.button(label, null, 150, onClick);
        }

        public Builder button(final String label, final @Nullable String tooltip, final int width, final Runnable onClick) {
            return this.button(Component.text(label), tooltip == null ? null : Component.text(tooltip), width, onClick);
        }

        public Builder button(final Component label, final Runnable onClick) {
            return this.button(label, null, 150, onClick);
        }

        public Builder button(final Component label, final @Nullable Component tooltip, final int width, final Runnable onClick) {
            this.actions.add(new DialogAction(label, tooltip, width, onClick));
            return this;
        }

        public ActionBar build() {
            return new ActionBar(this.actions);
        }
    }
}
