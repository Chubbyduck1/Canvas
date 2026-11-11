package me.chubbyduck.canvas.elements.display;

import java.util.Objects;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Justify;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A settings row: a label and muted description on the left, a control on the right. */
@NullMarked
public class SettingRow extends Composite {

    private final Component label;
    private final @Nullable Component description;
    private final NodeLike control;
    private final int descriptionWidth;

    private SettingRow(final Builder builder) {
        this.label = Objects.requireNonNull(builder.label, "SettingRow needs label(...)");
        this.description = builder.description;
        this.control = Objects.requireNonNull(builder.control, "SettingRow needs control(...)");
        this.descriptionWidth = builder.descriptionWidth;
        this.width(Length.FILL);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Node build() {
        final Flex.Builder text = Column.builder().gap(1).grow(1).add(Text.of(this.label));
        if (this.description != null) {
            final Text description = Text.muted(this.description);
            if (this.descriptionWidth > 0) {
                description.maxWidth(this.descriptionWidth);
            }
            text.add(description);
        }
        return Row.builder()
            .width(Length.FILL)
            .justify(Justify.SPACE_BETWEEN)
            .align(Align.CENTER)
            .gap(8)
            .add(text.build())
            .add(this.control)
            .build();
    }

    public static class Builder {

        private @Nullable Component label;
        private @Nullable Component description;
        private @Nullable NodeLike control;
        private int descriptionWidth;

        public Builder label(final String label) {
            this.label = Component.text(label);
            return this;
        }

        /** A styled label. */
        public Builder label(final Component label) {
            this.label = label;
            return this;
        }

        public Builder description(final @Nullable String description) {
            this.description = description == null ? null : Component.text(description);
            return this;
        }

        /** A styled description. */
        public Builder description(final @Nullable Component description) {
            this.description = description;
            return this;
        }

        public Builder control(final NodeLike control) {
            this.control = control;
            return this;
        }

        /** Caps the description's width. */
        public Builder descriptionWidth(final int descriptionWidth) {
            this.descriptionWidth = descriptionWidth;
            return this;
        }

        public SettingRow build() {
            return new SettingRow(this);
        }
    }
}
