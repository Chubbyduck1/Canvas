package me.chubbyduck.canvas.kitchensink.pages;

import me.chubbyduck.canvas.elements.control.Stepper;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.node.Gauge;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/**
 * Shows the kitchensink's own primitive, {@link Gauge}, which registers its textures itself.
 */
@NullMarked
public class GaugePage implements NodeLike {

    @Override
    public Node getNode(final Scope scope) {
        final State<Integer> level = scope.useState(5);
        return PageChrome.page(scope, "Custom node", "Gauge extends Node, overrides register(...) and is listed at bootstrap.",
            Panel.builder()
                .title("Gauge")
                .add(Row.builder()
                    .gap(8)
                    .align(Align.CENTER)
                    .add(new Gauge(level.get()))
                    .add(Stepper.builder().value(level).range(0, Gauge.LEVELS).build())
                    .build())
                .add(Text.muted("Its 9 sprites are painted from the theme at startup."))
                .build()
        );
    }
}
