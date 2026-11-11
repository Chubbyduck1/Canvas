package me.chubbyduck.canvas.kitchensink.pages;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.control.Tabs;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.Kitchensink;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/** Lists every page, grouped. */
@NullMarked
public class IndexPage implements NodeLike {

    @Override
    public Node getNode(final Scope scope) {
        final Set<String> groups = new LinkedHashSet<>();
        for (final Kitchensink.Page page : Kitchensink.pages()) {
            groups.add(page.group());
        }
        final State<String> group = scope.useState(groups.iterator().next());

        final Flex.Builder buttons = Row.builder().wrap(true).gap(3);
        final List<Kitchensink.Page> visible = Kitchensink.pages().stream()
            .filter(page -> page.group().equals(group.get()))
            .toList();
        for (final Kitchensink.Page page : visible) {
            buttons.add(Button.builder()
                .label(page.title())
                .minWidth(72)
                .tooltip("/ks " + page.id())
                .onClick(() -> Kitchensink.canvas(page).open(scope.viewer()))
                .build());
        }

        return PageChrome.surface(Kitchensink.WIDTH, Row.builder()
            .gap(6)
            .add(Tabs.<String>builder()
                .tabs(List.copyOf(groups))
                .label(name -> Character.toUpperCase(name.charAt(0)) + name.substring(1))
                .value(group)
                .buttonWidth(76)
                .build())
            .add(Column.builder()
                .grow(1)
                .gap(6)
                .align(Align.STRETCH)
                .add(Text.title("Canvas Kitchensink"))
                .add(Text.muted("Every element and feature, one page each. Click a page; the arrow on each page comes back here.")
                    .maxWidth(230))
                .add(Panel.builder().title(group.get() + " (" + visible.size() + ")").add(buttons.build()).build())
                .build())
            .build());
    }
}
