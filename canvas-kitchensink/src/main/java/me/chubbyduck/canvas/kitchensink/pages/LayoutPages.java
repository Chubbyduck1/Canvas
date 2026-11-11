package me.chubbyduck.canvas.kitchensink.pages;

import java.util.Locale;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.control.Segmented;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Spacer;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Absolute;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.elements.layout.Stack;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Justify;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/** Pages for the containers: absolute positioning, flex, stack, nesting and offsets. */
@NullMarked
public final class LayoutPages {

    private LayoutPages() {
    }

    public static class AbsolutePage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Absolute", "Children at exact pixels, overlapping; later ones draw on top.",
                Absolute.builder()
                    .size(320, 110)
                    .add(Panel.builder().title("Back panel").build(), Absolute.at(0, 0).size(200, 80))
                    .add(Panel.builder().title("Front panel, overlapping").build(), Absolute.at(90, 30).size(220, 76))
                    .add(Button.of("At (13, 57)", () -> {
                    }), Absolute.at(13, 57))
                    .add(Text.of("Text at (101, 53), odd row offset"), Absolute.at(101, 53))
                    .build()
            );
        }
    }

    public static class FlexPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Justify> justify = scope.useState(Justify.START);
            final State<Align> align = scope.useState(Align.CENTER);
            final State<Boolean> wrap = scope.useState(false);
            final Flex.Builder demo = Row.builder()
                .width(Length.FILL)
                .height(48)
                .gap(3)
                .justify(justify.get())
                .align(align.get())
                .wrap(wrap.get());
            for (int i = 1; i <= (wrap.get() ? 9 : 4); i++) {
                demo.add(Button.of("Item " + i, () -> {
                }).height(16 + (i % 3) * 4));
            }
            return PageChrome.page(scope, "Flex", "Justify, align and wrap, live.",
                Segmented.<Justify>builder()
                    .options(Justify.values())
                    .label(option -> option.name().replace("SPACE_", "").toLowerCase(Locale.ROOT))
                    .value(justify)
                    .build(),
                Segmented.<Align>builder().options(Align.values()).label(option -> option.name().toLowerCase(Locale.ROOT)).value(align).build(),
                Segmented.<Boolean>builder().options(false, true).label(wrapping -> wrapping ? "wrap" : "no wrap").value(wrap).build(),
                Panel.builder().add(demo.build()).build(),
                Row.builder()
                    .width(Length.FILL)
                    .gap(3)
                    .add(Button.of("grow 1", () -> {
                    }).grow(1))
                    .add(Button.of("grow 2", () -> {
                    }).grow(2))
                    .add(Button.of("fixed", () -> {
                    }))
                    .build()
            );
        }
    }

    public static class StackPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Stack", "Children layered and aligned inside the stack.",
                Stack.builder()
                    .add(Panel.builder().title("Under").build().fill())
                    .add(Stack.builder()
                        .center()
                        .add(Button.of("Centered on top", () -> {
                        }))
                        .build()
                        .fill())
                    .build()
                    .height(80)
                    .width(Length.FILL)
            );
        }
    }

    public static class NestedPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Nested", "Absolute inside flex inside absolute.",
                Absolute.builder()
                    .size(320, 100)
                    .add(Row.builder()
                        .gap(4)
                        .add(Panel.builder().title("Flex child").add(Text.of("in a row")).build())
                        .add(Absolute.builder()
                            .size(120, 60)
                            .add(Panel.builder().build(), Absolute.at(0, 0).size(120, 60))
                            .add(Text.of("abs in flex"), Absolute.at(7, 23))
                            .build())
                        .build(), Absolute.at(5, 5))
                    .build()
            );
        }
    }

    public static class OffsetPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Offset", "offset(dx, dy) moves one child without moving its siblings.",
                Row.builder()
                    .gap(4)
                    .add(Button.of("A", () -> {
                    }))
                    .add(Button.of("B offset(0, 5)", () -> {
                    }).offset(0, 5))
                    .add(Button.of("C", () -> {
                    }))
                    .build(),
                // Offsets don't take up layout space, so leave room for B's 5px shift.
                Spacer.of(0, 5)
            );
        }
    }

    /** The same panel built twice: absolutely and with flex. */
    public static class AbsoluteVsFlexPage implements NodeLike {

        public static Node absoluteVersion() {
            return Absolute.builder()
                .size(150, 40)
                .add(Button.of("One", () -> {
                }), Absolute.at(0, 0).size(70, 20))
                .add(Button.of("Two", () -> {
                }), Absolute.at(74, 0).size(70, 20))
                .add(Text.of("Below"), Absolute.at(0, 24))
                .build();
        }

        public static Node flexVersion() {
            return Column.builder()
                .width(150)
                .height(40)
                .gap(4)
                .add(Row.builder()
                    .gap(4)
                    .add(Button.of("One", () -> {
                    }).width(70))
                    .add(Button.of("Two", () -> {
                    }).width(70))
                    .build())
                .add(Text.of("Below"))
                .build();
        }

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Absolute vs Flex", "Left: absolute. Right: flex. They should look identical.",
                Row.builder().gap(10).add(absoluteVersion()).add(flexVersion()).build()
            );
        }
    }
}
