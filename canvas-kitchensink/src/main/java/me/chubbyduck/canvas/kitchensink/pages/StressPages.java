package me.chubbyduck.canvas.kitchensink.pages;

import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.layout.Absolute;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/**
 * Pages that push the glyph compiler: lots of glyphs, lots of click regions.
 */
@NullMarked
public final class StressPages {

    private StressPages() {
    }

    public static class ManyGlyphsPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final Flex.Builder grid = Row.builder().wrap(true).gap(2).width(320);
            for (int i = 0; i < 60; i++) {
                grid.add(Button.builder().label(Integer.toString(i)).selected(i % 7 == 0).minWidth(24).build());
            }
            return PageChrome.page(scope, "Many glyphs", "60 buttons; /ks selftest reports the payload size.", grid.build());
        }
    }

    public static class ManyHitRegionsPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> last = scope.useState(-1);
            final Absolute.Builder cells = Absolute.builder().size(320, 90);
            int index = 0;
            for (int y = 0; y < 90; y += 9) {
                for (int x = 0; x < 320; x += 16) {
                    final int id = index++;
                    cells.add(new Cell(id, last), Absolute.at(x, y).size(15, 9));
                }
            }
            return PageChrome.page(scope, "Many hits", "200 click regions, one per 16x9 cell. Last clicked: " + last.get(),
                cells.build());
        }

        /**
         * An invisible clickable cell.
         */
        static class Cell extends Node {

            private final int id;
            private final State<Integer> last;

            Cell(final int id, final State<Integer> last) {
                this.id = id;
                this.last = last;
            }

            @Override
            protected void paint(final me.chubbyduck.canvas.paint.PaintContext context) {
                if (this.last.get() == this.id) {
                    context.text("x", context.rect().x() + 5, context.rect().y(), context.theme().palette().accent(), true);
                } else if (this.id % 2 == 0) {
                    context.text(".", context.rect().x() + 6, context.rect().y(), context.theme().palette().textMuted(), false);
                }
                context.hit(context.rect(), () -> this.last.set(this.id));
            }
        }
    }
}
