package me.chubbyduck.canvas.kitchensink.pages;

import java.util.List;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.elements.control.ArrowButton;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.control.Dropdown;
import me.chubbyduck.canvas.elements.control.Segmented;
import me.chubbyduck.canvas.elements.control.Slider;
import me.chubbyduck.canvas.elements.control.Stepper;
import me.chubbyduck.canvas.elements.control.Tabs;
import me.chubbyduck.canvas.elements.control.Toggle;
import me.chubbyduck.canvas.elements.display.Divider;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.SettingRow;
import me.chubbyduck.canvas.elements.display.Spacer;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.kitchensink.Kitchensink;
import me.chubbyduck.canvas.kitchensink.node.Flags;
import me.chubbyduck.canvas.kitchensink.node.Gauge;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;

/**
 * An OreUI-style settings screen: sidebar tabs, and on the "Interface" tab a game-settings screen that uses as many
 * elements as possible (dropdown with icons, segmented buttons, stepper, slider, toggles, the custom Gauge, styled
 * text, buttons, divider).
 */
@NullMarked
public class ReferenceSettingsPage implements NodeLike {

    public static final int WIDTH = Canvas.MAX_WIDTH;

    /**
     * Minimum height: the tallest tab's (Interface) natural height, so every tab opens the same size and the sidebar
     * (and Reset) never moves.
     */
    public static final int HEIGHT = 378;

    private static final List<String> TABS = List.of(
        "General", "Interface", "Notifications", "Resource Pack", "Extensions", "Language");
    private static final List<String> DIFFICULTIES = List.of("Peaceful", "Easy", "Normal", "Hard");
    private static final int XP_GOAL = 5000;

    @Override
    public Node getNode(final Scope scope) {
        final State<String> tab = scope.useState("Interface");
        final State<Flags.Language> language = scope.useState(Flags.Language.PL);
        final State<String> units = scope.useState("Metric");
        final State<Integer> difficulty = scope.useState(2);
        final State<Integer> renderDistance = scope.useState(12);
        final State<Boolean> autoJump = scope.useState(false);
        final State<Boolean> subtitles = scope.useState(true);
        final State<Integer> experience = scope.useState(3400);
        final State<Boolean> claimed = scope.useState(false);
        final State<Integer> stamina = scope.useState(5);

        final Runnable reset = () -> {
            language.set(Flags.Language.PL);
            units.set("Metric");
            difficulty.set(2);
            renderDistance.set(12);
            autoJump.set(false);
            subtitles.set(true);
            experience.set(3400);
            claimed.set(false);
            stamina.set(5);
        };

        final NodeLike content = tab.get().equals("Interface")
            ? this.interfaceTab(language, units, difficulty, renderDistance, autoJump, subtitles, experience, claimed, stamina)
            : Panel.builder().title(tab.get()).add(Text.muted("Nothing to configure here yet.")).build().grow(1);

        // The right column follows the sidebar's grid: a 20px title row level with the search
        // button, then the cards starting level with the first tab, ending level with Reset.
        // grow(1): fill the whole page height, so Reset stays at the bottom even when a tab's
        // content is shorter than the canvas (which never shrinks while open).
        return PageChrome.surface(WIDTH, Row.builder()
            .grow(1)
            .gap(6)
            .align(Align.STRETCH)
            .add(Column.builder()
                .width(84)
                .gap(2)
                .align(Align.STRETCH)
                .add(Row.builder()
                    .gap(2)
                    .align(Align.CENTER)
                    .add(ArrowButton.left(() -> Kitchensink.index().open(scope.viewer())))
                    .add(Button.builder().label("Search...").build().grow(1))
                    .build())
                .add(Spacer.of(0, 4))
                .add(Tabs.<String>builder().tabs(TABS).value(tab).build())
                .add(Spacer.flexible())
                .add(Button.builder().label("Reset").tooltip("Restore every setting").onClick(reset).build())
                .build())
            .add(Column.builder()
                .grow(1)
                .gap(2)
                .align(Align.STRETCH)
                .add(Row.builder().height(20).align(Align.CENTER).add(Text.of(tab.get())).build())
                .add(Spacer.of(0, 4))
                .add(content)
                .build())
            .build()).minHeight(HEIGHT);
    }

    private NodeLike interfaceTab(
        final State<Flags.Language> language,
        final State<String> units,
        final State<Integer> difficulty,
        final State<Integer> renderDistance,
        final State<Boolean> autoJump,
        final State<Boolean> subtitles,
        final State<Integer> experience,
        final State<Boolean> claimed,
        final State<Integer> stamina
    ) {
        final Panel region = Panel.builder()
            .title("Language & Region")
            .add(SettingRow.builder()
                .label("Language")
                .control(Dropdown.<Flags.Language>builder()
                    .options(Flags.Language.values())
                    .label(Flags.Language::display)
                    .icon(Flags::of)
                    .value(language)
                    .width(112)
                    .build())
                .build())
            .add(SettingRow.builder()
                .label("Units")
                .control(Segmented.<String>builder().options("Metric", "Imperial").value(units).build())
                .build())
            .add(SettingRow.builder()
                .label("Subtitles")
                .control(Toggle.of(subtitles))
                .build())
            .build();

        final int level = 12 + experience.get() / XP_GOAL;
        final Panel profile = Panel.builder()
            .title("Profile")
            .add(Row.builder()
                .gap(6)
                .align(Align.CENTER)
                .add(Text.of(Component.text("Lv. " + level, NamedTextColor.DARK_GREEN, TextDecoration.BOLD)))
                .add(Text.of(Component.text(String.format("%,d", experience.get() % XP_GOAL))
                    .append(Component.text(String.format(" / %,d XP", XP_GOAL), NamedTextColor.DARK_GRAY))))
                .build())
            .add(Row.builder()
                .gap(6)
                .align(Align.CENTER)
                .add(Text.muted("Stamina"))
                .add(new Gauge(stamina.get()))
                .add(Stepper.builder().value(stamina).range(0, Gauge.LEVELS).valueWidth(14).build())
                .build())
            .add(Divider.create())
            .add(Row.builder()
                .gap(4)
                .add(Button.builder()
                    .label(claimed.get() ? "Claimed" : "Claim reward")
                    .selected(!claimed.get())
                    .disabled(claimed.get())
                    .tooltip("+1,000 XP once")
                    .onClick(() -> {
                        claimed.set(true);
                        experience.update(current -> current + 1000);
                    })
                    .build()
                    .grow(1))
                .add(Button.builder()
                    .label("+250 XP")
                    .onClick(() -> experience.update(current -> current + 250))
                    .build())
                .build())
            .build();

        final Panel gameplay = Panel.builder()
            .title("Gameplay")
            .add(SettingRow.builder()
                .label("Difficulty")
                .description("Changes mob damage and hunger.")
                .control(Stepper.builder()
                    .value(difficulty)
                    .range(0, DIFFICULTIES.size() - 1)
                    .format(DIFFICULTIES::get)
                    .valueWidth(50)
                    .build())
                .build())
            .add(SettingRow.builder()
                .label("Render distance")
                .description("How far away chunks are drawn.")
                .control(Slider.builder()
                    .value(renderDistance)
                    .range(2, 32)
                    .step(2)
                    .format(chunks -> chunks + " chunk" + (chunks != 1 ? "s" : ""))
                    .trackWidth(72)
                    .build())
                .build())
            .add(SettingRow.builder()
                .label("Auto-jump")
                .description("Step up blocks without pressing jump.")
                .control(Toggle.of(autoJump))
                .build())
            .build();

        return Column.builder()
            .grow(1)
            .gap(4)
            .align(Align.STRETCH)
            .add(region.grow(1))
            .add(profile.grow(1))
            .add(gameplay.grow(1))
            .build();
    }
}
