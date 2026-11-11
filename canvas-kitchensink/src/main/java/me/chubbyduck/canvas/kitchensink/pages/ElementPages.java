package me.chubbyduck.canvas.kitchensink.pages;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import me.chubbyduck.canvas.elements.control.ArrowButton;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.control.Dropdown;
import me.chubbyduck.canvas.elements.control.Pager;
import me.chubbyduck.canvas.elements.control.Segmented;
import me.chubbyduck.canvas.elements.control.Slider;
import me.chubbyduck.canvas.elements.control.Stepper;
import me.chubbyduck.canvas.elements.control.Tabs;
import me.chubbyduck.canvas.elements.control.Toggle;
import me.chubbyduck.canvas.elements.display.Divider;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.SettingRow;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.input.ActionBar;
import me.chubbyduck.canvas.elements.input.NativeInput;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.node.Flags;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.NullMarked;

/** One page per built-in element, showing each of its states. */
@NullMarked
public final class ElementPages {

    private ElementPages() {
    }

    enum Mode {
        ON,
        MINIMAL,
        OFF
    }

    public static class TextPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Text", "Default font, 9px lines. Wraps at maxWidth; non-ASCII shows as ?.",
                Panel.builder()
                    .title("Styles")
                    .add(Text.of("Normal text"))
                    .add(Text.title("Bold title"))
                    .add(Text.muted("Muted description text"))
                    .add(Text.of("Custom color").color(0x3C8527))
                    .add(Text.of("Unicode: héllo wörld"))
                    .build(),
                Panel.builder()
                    .title("Adventure components")
                    .add(Text.of(Component.text("Balance: ")
                        .append(Component.text("$1,200", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                        .append(Component.text(" (+5%)", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC))))
                    .add(Text.of(Component.text()
                        .append(Component.text("Underlined", Style.style(TextDecoration.UNDERLINED)))
                        .append(Component.text(", "))
                        .append(Component.text("struck", Style.style(TextDecoration.STRIKETHROUGH)))
                        .append(Component.text(", "))
                        .append(Component.text("red", NamedTextColor.DARK_RED))
                        .append(Component.text(" and "))
                        .append(Component.text("blue", NamedTextColor.DARK_BLUE))
                        .build()))
                    .add(Text.of(Component.text("Styles carry across wrapped lines: ")
                        .append(Component.text("this whole bold purple sentence keeps its style after the break.",
                            NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))))
                    .build(),
                Panel.builder()
                    .title("Wrapping and alignment")
                    .add(Text.of("This paragraph wraps at 200 pixels so long sentences break onto new lines at word boundaries.")
                        .maxWidth(200))
                    .add(Text.of("Centered").align(Align.CENTER))
                    .add(Text.of("Right aligned").align(Align.END))
                    .build()
            );
        }
    }

    /** Object components: atlas sprites and player heads, drawn by the client inline with text. */
    public static class ObjectsPage implements NodeLike {

        private static final Key GUI = Key.key("minecraft", "gui");

        private static Component block(final String name) {
            return Component.object(ObjectContents.sprite(Key.key("block/" + name)));
        }

        private static Component gui(final String sprite) {
            return Component.object(ObjectContents.sprite(GUI, Key.key(sprite)));
        }

        private static Component head(final String player) {
            return Component.object(ObjectContents.playerHead(player));
        }

        private static Component repeat(final Component part, final int times) {
            final var out = Component.text();
            for (int i = 0; i < times; i++) {
                out.append(part);
            }
            return out.build();
        }

        @Override
        public Node getNode(final Scope scope) {
            final State<Boolean> hearts = scope.useState(true);
            final Component you = scope.viewer().get(Identity.NAME)
                .map(ObjectsPage::head)
                .or(() -> scope.viewer().get(Identity.UUID).map(id -> Component.object(ObjectContents.playerHead(id))))
                .orElse(head("Steve"));
            final String name = scope.viewer().get(Identity.NAME).orElse("you");

            return PageChrome.page(scope, "Objects", "Atlas sprites and player heads, drawn by the client. Text with objects snaps to the 9px row grid.",
                Panel.builder()
                    .title("Atlas sprites")
                    .add(Text.of(Component.text()
                        .append(block("diamond_block")).append(Component.text(" Diamond  "))
                        .append(block("emerald_block")).append(Component.text(" Emerald  "))
                        .append(block("gold_block")).append(Component.text(" Gold  "))
                        .append(block("oak_log")).append(Component.text(" Oak  "))
                        .append(block("tnt_side")).append(Component.text(" TNT"))
                        .build()))
                    .add(Text.of(Component.text("Mine ")
                        .append(block("diamond_ore"))
                        .append(Component.text(" with an iron pickaxe to get "))
                        .append(Component.text("3 diamonds", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                        .append(Component.text("."))))
                    .add(Text.of(Component.text("GUI atlas: ", NamedTextColor.DARK_GRAY)
                        .append(repeat(gui("hud/heart/full"), 3))
                        .append(gui("hud/heart/half"))
                        .append(Component.text("  "))
                        .append(repeat(gui("hud/food_full"), 4))
                        .append(Component.text("  "))
                        .append(repeat(gui("hud/armor_full"), 2))))
                    .build(),
                Panel.builder()
                    .title("Player heads")
                    .add(Text.of(Component.text()
                        .append(you)
                        .append(Component.text(" " + name, NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                        .append(Component.text(" (your skin)", NamedTextColor.DARK_GRAY))
                        .build()))
                    .add(Text.of(Component.text()
                        .append(head("Notch"))
                        .append(Component.text(" Notch joined the game", NamedTextColor.GOLD))
                        .build()))
                    .add(Text.of(Component.text()
                        .append(Component.object(ObjectContents.playerHead().name("jeb_").hat(false).build()))
                        .append(Component.text(" <jeb_> no hat layer on this one"))
                        .build()))
                    .build(),
                Panel.builder()
                    .title("In other elements")
                    .add(SettingRow.builder()
                        .label(Component.text().append(gui("hud/heart/full")).append(Component.text(" Show health")).build())
                        .description(Component.text("Labels and descriptions are Text, so objects line up."))
                        .control(Toggle.of(hearts))
                        .build())
                    .build()
            );
        }
    }

    public static class IconPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Icon", "Sprites registered by nodes, drawn at their natural size.",
                Panel.builder()
                    .title("Arrow sprites (ArrowButton's), at odd offsets")
                    .add(Row.builder()
                        .gap(5)
                        .align(Align.CENTER)
                        .add(ArrowButton.left(() -> {
                        }))
                        .add(Text.of("ArrowButton"))
                        .add(ArrowButton.right(() -> {
                        }).sized(14))
                        .add(Text.of("14px"))
                        .build())
                    .build()
            );
        }
    }

    public static class PanelPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            return PageChrome.page(scope, "Panel", "Outlined surface with an optional header; content stacks in a column.",
                Panel.builder().title("With a header").add(Text.of("Content")).build(),
                Panel.builder().add(Text.of("Without a header")).build(),
                Panel.builder()
                    .title("Nested")
                    .add(Panel.builder().title("Inner").add(Text.muted("Panels nest freely")).build())
                    .build()
            );
        }
    }

    public static class ButtonPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> clicks = scope.useState(0);
            return PageChrome.page(scope, "Button", "Normal, selected and disabled. Text can't change on hover, so glyph buttons have no hover state.",
                Panel.builder()
                    .title("States")
                    .add(Row.builder()
                        .gap(4)
                        .add(Button.builder().label("Normal").onClick(() -> clicks.update(current -> current + 1)).tooltip("Counts clicks").build())
                        .add(Button.builder().label("Selected").selected(true).onClick(() -> clicks.update(current -> current + 1)).build())
                        .add(Button.builder().label("Disabled").disabled(true).build())
                        .build())
                    .add(Text.of("Clicks: " + clicks.get()))
                    .build(),
                Panel.builder()
                    .title("Sizes")
                    .add(Row.builder()
                        .gap(4)
                        .align(Align.CENTER)
                        .add(Button.of("Tall", () -> {
                        }).height(30))
                        .add(Button.of("Wide", () -> {
                        }).width(120))
                        .add(Button.of("x", () -> {
                        }))
                        .build())
                    .build(),
                Panel.builder()
                    .title(Component.text("Styled ").append(Component.text("labels", NamedTextColor.DARK_GREEN)))
                    .add(Row.builder()
                        .gap(4)
                        .add(Button.builder()
                            .label(Component.text("Buy ").append(Component.text("$250", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)))
                            .tooltip(Component.text("Costs ").append(Component.text("$250", NamedTextColor.GREEN)))
                            .onClick(() -> clicks.update(current -> current + 1))
                            .build())
                        .add(Button.of(Component.text("Delete", NamedTextColor.DARK_RED), () -> clicks.update(current -> current + 1)))
                        .build())
                    .add(SettingRow.builder()
                        .label(Component.text("Rarity"))
                        .description(Component.text("Shown as ").append(Component.text("Epic", NamedTextColor.DARK_PURPLE)))
                        .control(Toggle.of(clicks.get() % 2 == 0, enabled -> clicks.update(current -> current + 1))
                            .labels(Component.text("Yes", NamedTextColor.DARK_GREEN), Component.text("No", NamedTextColor.DARK_RED)))
                        .build())
                    .build()
            );
        }
    }

    public static class SegmentedPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Mode> mode = scope.useState(Mode.ON);
            final State<String> size = scope.useState("M");
            return PageChrome.page(scope, "Segmented", "One selected option, bound to state.",
                Panel.builder()
                    .title("Bound to State<Mode>")
                    .add(Segmented.<Mode>builder()
                        .options(Mode.values())
                        .label(option -> option.name().charAt(0) + option.name().substring(1).toLowerCase(Locale.ROOT))
                        .value(mode)
                        .build())
                    .add(Text.muted("Selected: " + mode.get()))
                    .build(),
                Panel.builder()
                    .title("Controlled (value + onChange)")
                    .add(Segmented.<String>builder()
                        .options("S", "M", "L", "XL")
                        .value(size.get(), size::set)
                        .build())
                    .build()
            );
        }
    }

    public static class TabsPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<String> tab = scope.useState("General");
            final State<String> top = scope.useState("One");
            return PageChrome.page(scope, "Tabs", "Vertical (sidebar) and horizontal tab lists.",
                Row.builder()
                    .gap(8)
                    .add(Tabs.<String>builder()
                        .tabs("General", "Interface", "Audio")
                        .value(tab)
                        .buttonWidth(80)
                        .build())
                    .add(Column.builder()
                        .gap(6)
                        .add(Tabs.<String>builder().tabs("One", "Two", "Three").value(top).vertical(false).build())
                        .add(Text.of("Sidebar: " + tab.get() + ", top: " + top.get()))
                        .build())
                    .build()
            );
        }
    }

    public static class StepperPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> amount = scope.useState(3);
            return PageChrome.page(scope, "Stepper", "Arrow buttons step a number; arrows disable at the limits.",
                Panel.builder()
                    .title("0..5")
                    .add(Stepper.builder().value(amount).range(0, 5).format(value -> value + " / 5").build())
                    .build()
            );
        }
    }

    public static class SliderPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> opacity = scope.useState(70);
            final State<Integer> fine = scope.useState(5);
            return PageChrome.page(scope, "Slider", "Glyph slider: click anywhere on the track (every step is a click region) or use the arrows.",
                Panel.builder()
                    .title("0..100 step 10, with label")
                    .add(Slider.builder().value(opacity).range(0, 100).step(10).format(value -> value + "%").trackWidth(130).build())
                    .build(),
                Panel.builder()
                    .title("0..20 step 1, no arrows")
                    .add(Slider.builder().value(fine).range(0, 20).arrows(false).trackWidth(200).build())
                    .add(Text.muted("Value: " + fine.get()))
                    .build()
            );
        }
    }

    public static class TogglePage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Boolean> moving = scope.useState(false);
            final State<Boolean> sound = scope.useState(true);
            return PageChrome.page(scope, "Toggle", "Glyph switch; click anywhere on it to flip.",
                Panel.builder()
                    .title("Switches")
                    .add(Row.builder().gap(8).align(Align.CENTER).add(Text.of("Moving background")).add(Toggle.of(moving)).build())
                    .add(Row.builder().gap(8).align(Align.CENTER).add(Text.of("Sound, no label")).add(Toggle.of(sound).labels("", "")).build())
                    .build()
            );
        }
    }

    public static class TextFieldPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<String> name = scope.useState("Steve");
            return PageChrome.page(scope, "Text field", "Native text input; it appears below the canvas. Click Apply to send its value.",
                Panel.builder().title("Current value").add(Text.of("Name: " + name.get())).build(),
                NativeInput.text("Name", name),
                ActionBar.builder().button("Apply", () -> {
                }).build()
            );
        }
    }

    public static class DropdownPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Flags.Language> language = scope.useState(Flags.Language.PL);
            final State<String> fruit = scope.useState("Apple");
            final State<String> difficulty = scope.useState("normal");
            final Map<String, String> options = new LinkedHashMap<>();
            options.put("peaceful", "Peaceful");
            options.put("easy", "Easy");
            options.put("normal", "Normal");
            options.put("hard", "Hard");
            return PageChrome.page(scope, "Dropdown",
                "Drawn dropdowns open over the content below (upward if there's no room). The native option button sits below the canvas.",
                Panel.builder()
                    .title("Drawn dropdowns")
                    .add(SettingRow.builder()
                        .label("Language")
                        .description("With icons: " + language.get().display())
                        .control(Dropdown.<Flags.Language>builder()
                            .options(Flags.Language.values())
                            .label(Flags.Language::display)
                            .icon(Flags::of)
                            .value(language)
                            .width(112)
                            .build())
                        .build())
                    .add(SettingRow.builder()
                        .label("Fruit")
                        .description("Text only, sized to its options: " + fruit.get())
                        .control(Dropdown.<String>builder().options("Apple", "Banana", "Cherry").value(fruit).build())
                        .build())
                    .build(),
                Panel.builder().title("Native option button").add(Text.of("Difficulty: " + difficulty.get())).build(),
                NativeInput.options("Difficulty", options, difficulty),
                ActionBar.builder().button("Apply", () -> {
                }).build()
            );
        }
    }

    public static class SettingRowPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Boolean> sidebar = scope.useState(true);
            final State<Integer> volume = scope.useState(50);
            return PageChrome.page(scope, "Setting row", "Label and description on the left, any control on the right.",
                Panel.builder()
                    .title("Rows")
                    .add(SettingRow.builder().label("Sidebar").description("Show the sidebar information.").control(Toggle.of(sidebar)).build())
                    .add(Divider.create())
                    .add(SettingRow.builder()
                        .label("Volume")
                        .description("A longer description that wraps onto a second line.")
                        .control(Slider.builder().value(volume).range(0, 100).step(10).arrows(false).trackWidth(70).build())
                        .build())
                    .build()
            );
        }
    }

    public static class PagerPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Integer> page = scope.useState(0);
            return PageChrome.page(scope, "Pager", "One page at a time instead of scrolling.",
                Panel.builder()
                    .title("Pages")
                    .add(Pager.builder()
                        .page(page)
                        .pages(Text.of("First page"), Text.of("Second page"), Text.of("Third and last page"))
                        .build())
                    .build()
            );
        }
    }

    public static class ActionBarPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<String> last = scope.useState("nothing yet");
            return PageChrome.page(scope, "Action bar", "Native footer buttons below the canvas.",
                Panel.builder().title("Last clicked").add(Text.of(last.get())).build(),
                ActionBar.builder()
                    .button("Reset", () -> last.set("Reset"))
                    .button("Done", "Also a tooltip", 150, () -> last.set("Done"))
                    .build()
            );
        }
    }
}
