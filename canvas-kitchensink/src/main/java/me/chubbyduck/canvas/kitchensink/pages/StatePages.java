package me.chubbyduck.canvas.kitchensink.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Pages for {@code useState}: updates, batching, passing state down, keyed children.
 */
@NullMarked
public final class StatePages {

    private StatePages() {
    }

    public static class CounterPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> count = scope.useState(0);
            return PageChrome.page(scope, "Counter", "set and update re-render the canvas.",
                Panel.builder()
                    .title("Count: " + count.get())
                    .add(Row.builder()
                        .gap(4)
                        .add(Button.of("-1", () -> count.update(current -> current - 1)))
                        .add(Button.of("+1", () -> count.update(current -> current + 1)))
                        .add(Button.of("Reset", () -> count.set(0)))
                        .build())
                    .build()
            );
        }
    }

    /**
     * One click sets state ten times; the render counter should go up by exactly one.
     */
    public static class BatchingPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> value = scope.useState(0);
            // A mutable holder in state: mutating it doesn't trigger a render, so it counts renders.
            final State<AtomicInteger> renders = scope.useState(AtomicInteger::new);
            final int renderCount = renders.get().incrementAndGet();
            return PageChrome.page(scope, "Batching", "Ten set() calls in one click cause one render.",
                Panel.builder()
                    .title("Value " + value.get() + ", renders " + renderCount)
                    .add(Button.of("Set 10 times", () -> {
                        for (int i = 0; i < 10; i++) {
                            value.update(current -> current + 1);
                        }
                    }))
                    .add(Text.muted("Renders should increase by 1 per click, value by 10."))
                    .build()
            );
        }
    }

    public static class SharedPropPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> shared = scope.useState(0);
            return PageChrome.page(scope, "Shared prop", "A parent's state passed to two child elements; both update.",
                Row.builder().gap(6).add(new Child("Left", shared)).add(new Child("Right", shared)).build()
            );
        }

        /**
         * A custom element receiving the parent's state as a constructor argument.
         */
        static class Child implements NodeLike {

            private final String name;
            private final State<Integer> shared;

            Child(final String name, final State<Integer> shared) {
                this.name = name;
                this.shared = shared;
            }

            @Override
            public Node getNode(final Scope scope) {
                return Panel.builder()
                    .title(this.name + ": " + this.shared.get())
                    .add(Button.of("+1 from " + this.name, () -> this.shared.update(current -> current + 1)))
                    .build();
            }
        }
    }

    /**
     * Each row has its own counter state; shuffling the keyed rows keeps each counter with its row.
     */
    public static class KeyedListPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<List<String>> order = scope.useState(List.of("Apple", "Banana", "Cherry"));
            final Flex.Builder rows = Column.builder().gap(3).align(Align.STRETCH);
            for (final String item : order.get()) {
                rows.add(new Item(item));
            }
            return PageChrome.page(scope, "Keyed list", "Click rows to count, then shuffle: counts follow their row's key.",
                Button.of("Shuffle", () -> {
                    final List<String> shuffled = new ArrayList<>(order.get());
                    Collections.rotate(shuffled, 1);
                    order.set(List.copyOf(shuffled));
                }),
                Panel.builder().add(rows.build()).build()
            );
        }

        static class Item implements NodeLike {

            private final String name;

            Item(final String name) {
                this.name = name;
            }

            @Override
            public Node getNode(final Scope scope) {
                final State<Integer> clicks = scope.useState(0);
                return Button.builder()
                    .label(this.name + " (" + clicks.get() + ")")
                    .onClick(() -> clicks.update(current -> current + 1))
                    .build();
            }

            @Override
            public @Nullable String key() {
                return this.name;
            }
        }
    }
}
