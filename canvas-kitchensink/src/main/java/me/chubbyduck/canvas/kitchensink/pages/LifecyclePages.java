package me.chubbyduck.canvas.kitchensink.pages;

import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.CanvasApi;
import me.chubbyduck.canvas.elements.control.ArrowButton;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.Kitchensink;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.session.CloseReason;
import me.chubbyduck.canvas.session.OpenMode;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Pages for opening, reopening, navigating and closing.
 */
@NullMarked
public final class LifecyclePages {

    private LifecyclePages() {
    }

    /**
     * Hops out to chat and back: the canvas captured with {@code Canvas.current} reopens with its state intact.
     */
    public static class ReopenPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> count = scope.useState(0);
            final State<String> name = scope.useState("(none)");
            return PageChrome.page(scope, "Reopen", "Change the count, ask in chat, answer: the page comes back with the same count.",
                Panel.builder()
                    .title("Count " + count.get() + ", name " + name.get())
                    .add(Row.builder()
                        .gap(4)
                        .add(Button.of("+1", () -> count.update(current -> current + 1)))
                        .add(Button.of("Ask in chat", () -> {
                            final Canvas canvas = CanvasApi.getCurrentCanvas(scope.viewer()).orElseThrow();
                            Kitchensink.hooks().askChat(scope.viewer(), "Type a name in chat", answer -> {
                                name.set(answer);
                                canvas.open(scope.viewer());
                            });
                        }))
                        .add(Button.of("Reopen fresh", () -> scope.canvas().open(scope.viewer(), OpenMode.FRESH)))
                        .build())
                    .add(Text.muted("/ks reopen also reopens the last closed canvas from outside a handler."))
                    .build()
            );
        }
    }

    /**
     * Forward and back across canvases.
     */
    public static class NavigationPage implements NodeLike {

        private final int level;
        private final @Nullable Canvas previous;

        public NavigationPage(final int level, final @Nullable Canvas previous) {
            this.level = level;
            this.previous = previous;
        }

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> count = scope.useState(0);
            final var row = Row.builder().gap(4).align(Align.CENTER);
            if (this.previous != null) {
                row.add(ArrowButton.left(() -> this.previous.open(scope.viewer())));
                row.add(Text.of("Back"));
            }
            row.add(Button.of("Count " + count.get(), () -> count.update(current -> current + 1)));
            if (this.level < 3) {
                row.add(Button.of("Next level", () -> {
                    final Canvas next = Kitchensink.decorate(
                        new Canvas(new NavigationPage(this.level + 1, scope.canvas())), "Navigation " + (this.level + 1));
                    next.open(scope.viewer());
                }));
            }
            return PageChrome.page(scope, "Navigation, level " + this.level, "Count here, go to the next level, come back: the count is kept.",
                Panel.builder().title("Level " + this.level + " of 3").add(row.build()).build()
            );
        }
    }

    /**
     * Shows why this canvas last closed.
     */
    public static class ClosePage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final CloseReason last = Kitchensink.lastReason(scope.viewer());
            return PageChrome.page(scope, "Close", "Close it a few ways, then reopen with /ks reopen to see the reason.",
                Panel.builder()
                    .title("Last close reason: " + (last == null ? "none yet" : last.name()))
                    .add(Row.builder()
                        .gap(4)
                        .add(Button.of("Close by code", () -> scope.session().close()))
                        .add(Button.of("Open inventory", () -> Kitchensink.hooks().openInventory(scope.viewer())))
                        .build())
                    .add(Text.muted("Escape gives ESCAPE; opening another page gives REPLACED."))
                    .build()
            );
        }
    }
}
