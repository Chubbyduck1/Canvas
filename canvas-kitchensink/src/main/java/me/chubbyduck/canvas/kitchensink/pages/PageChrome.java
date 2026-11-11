package me.chubbyduck.canvas.kitchensink.pages;

import me.chubbyduck.canvas.elements.control.ArrowButton;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.Kitchensink;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import org.jspecify.annotations.NullMarked;

/** Common page frame: a back arrow to the index, the page title, a one-line note, then content. */
@NullMarked
public final class PageChrome {

    private PageChrome() {
    }

    public static Node page(final Scope scope, final String title, final String note, final NodeLike... content) {
        final Panel.Builder column = Panel.builder()
            .padding(Insets.of(4, 5, 5, 5))
            .gap(6)
            .add(Row.builder()
                .gap(6)
                .align(Align.CENTER)
                .add(ArrowButton.left(() -> Kitchensink.index().open(scope.viewer())))
                .add(Column.builder()
                    .gap(1)
                    .add(Text.title(title))
                    .add(Text.muted(note).maxWidth(Kitchensink.WIDTH - 40))
                    .build())
                .build());
        column.add(content);
        return column.build().width(Kitchensink.WIDTH);
    }

    /**
     * A full-size panel surface behind a page's content, so text never sits directly on the dialog's dark background.
     */
    public static Node surface(final int width, final NodeLike content) {
        return Panel.builder().padding(4).add(content).build().width(width);
    }
}
