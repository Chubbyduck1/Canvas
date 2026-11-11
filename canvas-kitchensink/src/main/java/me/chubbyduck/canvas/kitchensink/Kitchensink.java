package me.chubbyduck.canvas.kitchensink;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.kitchensink.hooks.KitchensinkHooks;
import me.chubbyduck.canvas.kitchensink.pages.ElementPages;
import me.chubbyduck.canvas.kitchensink.pages.GaugePage;
import me.chubbyduck.canvas.kitchensink.pages.IndexPage;
import me.chubbyduck.canvas.kitchensink.pages.InputPages;
import me.chubbyduck.canvas.kitchensink.pages.LayoutPages;
import me.chubbyduck.canvas.kitchensink.pages.LifecyclePages;
import me.chubbyduck.canvas.kitchensink.pages.ReferenceSettingsPage;
import me.chubbyduck.canvas.kitchensink.pages.StatePages;
import me.chubbyduck.canvas.kitchensink.pages.StressPages;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.session.CloseReason;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** The page registry and the canvases built from it. */
@NullMarked
public final class Kitchensink {

    /** Width every page canvas uses, in GUI pixels. */
    public static final int WIDTH = 330;

    private static final Map<String, Canvas> CANVASES = new ConcurrentHashMap<>();
    private static final Map<UUID, Canvas> LAST_CLOSED = new ConcurrentHashMap<>();
    private static final Map<UUID, CloseReason> LAST_REASON = new ConcurrentHashMap<>();
    private static final List<Page> PAGES = List.of(
        new Page("text", "Text", "elements", ElementPages.TextPage::new),
        new Page("objects", "Objects", "elements", ElementPages.ObjectsPage::new),
        new Page("icon", "Icon", "elements", ElementPages.IconPage::new),
        new Page("panel", "Panel", "elements", ElementPages.PanelPage::new),
        new Page("button", "Button", "elements", ElementPages.ButtonPage::new),
        new Page("segmented", "Segmented", "elements", ElementPages.SegmentedPage::new),
        new Page("tabs", "Tabs", "elements", ElementPages.TabsPage::new),
        new Page("stepper", "Stepper", "elements", ElementPages.StepperPage::new),
        new Page("slider", "Slider", "elements", ElementPages.SliderPage::new),
        new Page("toggle", "Toggle", "elements", ElementPages.TogglePage::new),
        new Page("textfield", "Text field", "elements", ElementPages.TextFieldPage::new),
        new Page("dropdown", "Dropdown", "elements", ElementPages.DropdownPage::new),
        new Page("settingrow", "Setting row", "elements", ElementPages.SettingRowPage::new),
        new Page("pager", "Pager", "elements", ElementPages.PagerPage::new),
        new Page("actionbar", "Action bar", "elements", ElementPages.ActionBarPage::new),
        new Page("gauge", "Custom node", "elements", GaugePage::new),
        new Page("absolute", "Absolute", "layout", LayoutPages.AbsolutePage::new),
        new Page("flex", "Flex", "layout", LayoutPages.FlexPage::new),
        new Page("stack", "Stack", "layout", LayoutPages.StackPage::new),
        new Page("nested", "Nested", "layout", LayoutPages.NestedPage::new),
        new Page("offset", "Offset", "layout", LayoutPages.OffsetPage::new),
        new Page("absvsflex", "Abs vs Flex", "layout", LayoutPages.AbsoluteVsFlexPage::new),
        new Page("counter", "Counter", "state", StatePages.CounterPage::new),
        new Page("batching", "Batching", "state", StatePages.BatchingPage::new),
        new Page("sharedprop", "Shared prop", "state", StatePages.SharedPropPage::new),
        new Page("keyedlist", "Keyed list", "state", StatePages.KeyedListPage::new),
        new Page("reopen", "Reopen", "lifecycle", LifecyclePages.ReopenPage::new),
        new Page("navigation", "Navigation", "lifecycle", () -> new LifecyclePages.NavigationPage(1, null)),
        new Page("close", "Close", "lifecycle", LifecyclePages.ClosePage::new),
        new Page("inputsync", "Input sync", "inputs", InputPages.InputSyncPage::new),
        new Page("mixedinputs", "Mixed inputs", "inputs", InputPages.MixedInputsPage::new),
        new Page("manyglyphs", "Many glyphs", "stress", StressPages.ManyGlyphsPage::new),
        new Page("manyhits", "Many hits", "stress", StressPages.ManyHitRegionsPage::new),
        new Page("reference", "Reference", "reference", ReferenceSettingsPage::new)
    );
    private static volatile KitchensinkHooks hooks = KitchensinkHooks.NONE;

    private Kitchensink() {
    }

    public static List<Page> pages() {
        return PAGES;
    }

    public static Optional<Page> page(final String id) {
        return PAGES.stream().filter(page -> page.id().equals(id)).findFirst();
    }

    public static void hooks(final KitchensinkHooks value) {
        hooks = value;
    }

    public static KitchensinkHooks hooks() {
        return hooks;
    }

    /** The index canvas. */
    public static Canvas index() {
        return CANVASES.computeIfAbsent("index", id -> decorate(new Canvas(new IndexPage()), "Kitchensink"));
    }

    /** The canvas for a page. */
    public static Canvas canvas(final Page page) {
        return CANVASES.computeIfAbsent(page.id(), id -> decorate(new Canvas(page.factory().get()), page.title()));
    }

    /** Adds the kitchensink's title, width and close tracking to a canvas. */
    public static Canvas decorate(final Canvas canvas, final String title) {
        return canvas
            .title(Component.text("Canvas Kitchensink: " + title))
            .onClose((viewer, reason) -> {
                final UUID id = viewer.get(Identity.UUID).orElse(null);
                if (id != null) {
                    LAST_CLOSED.put(id, canvas);
                    LAST_REASON.put(id, reason);
                }
            });
    }

    public static Optional<Canvas> lastClosed(final Audience viewer) {
        return viewer.get(Identity.UUID).map(LAST_CLOSED::get);
    }

    public static @Nullable CloseReason lastReason(final Audience viewer) {
        return viewer.get(Identity.UUID).map(LAST_REASON::get).orElse(null);
    }

    public static void forget(final UUID player) {
        LAST_CLOSED.remove(player);
        LAST_REASON.remove(player);
    }

    /** A kitchensink page. */
    public record Page(String id, String title, String group, Supplier<NodeLike> factory) {
    }
}
