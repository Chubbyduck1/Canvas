package me.chubbyduck.canvas.elements.display;

import java.util.List;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Styled text from an Adventure {@link Component}: colors, bold, italic, underline, strikethrough and obfuscated can be
 * mixed freely.
 */
@NullMarked
public class Text extends Node {

    /**
     * Distance between lines.
     */
    public static final int LINE_HEIGHT = 9;

    private final Component content;
    private @Nullable Integer color;
    private Tone tone = Tone.NORMAL;
    private boolean bold;
    private int maxWidth;
    private Align align = Align.START;

    protected Text(final Component content) {
        this.content = content;
    }

    public static Text of(final Component content) {
        return new Text(content);
    }

    public static Text of(final String text) {
        return new Text(Component.text(text));
    }

    /**
     * Bold text in the primary color.
     */
    public static Text title(final String text) {
        return of(text).bold(true);
    }

    public static Text title(final Component content) {
        return of(content).bold(true);
    }

    /**
     * Text in the theme's secondary color.
     */
    public static Text muted(final String text) {
        return of(text).tone(Tone.MUTED);
    }

    public static Text muted(final Component content) {
        return of(content).tone(Tone.MUTED);
    }

    /**
     * Default color for parts of the text that don't set one.
     */
    public Text color(final int rgb) {
        this.color = rgb;
        return this;
    }

    /**
     * Default theme color for parts of the text that don't set one.
     */
    public Text tone(final Tone tone) {
        this.tone = tone;
        return this;
    }

    /**
     * Default boldness for parts of the text that don't set it.
     */
    public Text bold(final boolean bold) {
        this.bold = bold;
        return this;
    }

    /**
     * Caps the line width.
     */
    public Text maxWidth(final int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * Horizontal alignment of each line within the node's width.
     */
    public Text align(final Align align) {
        this.align = align;
        return this;
    }

    public Component content() {
        return this.content;
    }

    /**
     * The content with this node's defaults applied underneath it.
     */
    private Component styled(final int themeColor) {
        final Style defaults = Style.style()
            .color(TextColor.color(this.color != null ? this.color : themeColor))
            .decoration(TextDecoration.BOLD, this.bold)
            .build();
        return Component.text().style(defaults).append(this.content).build();
    }

    private int themeColor(final me.chubbyduck.canvas.asset.Theme theme) {
        return switch (this.tone) {
            case MUTED -> theme.palette().textMuted();
            case ON_ACCENT -> theme.palette().textOnAccent();
            case DISABLED -> theme.palette().textDisabled();
            case NORMAL -> theme.palette().text();
        } & 0xFFFFFF;
    }

    private List<Component> lines(final Component styled, final int available) {
        final int limit = this.maxWidth > 0 ? Math.min(this.maxWidth, available) : available;
        return StyledText.wrap(styled, limit <= 0 ? Integer.MAX_VALUE : limit);
    }

    @Override
    protected Size measure(final LayoutContext context) {
        final List<Component> lines = this.lines(this.styled(this.themeColor(context.theme())), context.maxWidth());
        int width = 0;
        for (final Component line : lines) {
            width = Math.max(width, context.textWidth(line));
        }
        // The last line's glyphs are 8px tall; the 9th pixel is the gap to the next line.
        final int height = lines.size() * LINE_HEIGHT - 1;
        // Objects only draw on the 9px row grid, so leave room to move down onto it.
        return new Size(width, StyledText.hasObjects(this.content) ? height + LINE_HEIGHT - 1 : height);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        final List<Component> lines = this.lines(this.styled(this.themeColor(context.theme())), Math.max(1, rect.width()));
        // Text with objects moves down onto the 9px row grid (it reserved the room in measure),
        // so the objects sit exactly where the client draws them.
        final int top = StyledText.hasObjects(this.content)
            ? rect.y() + Math.floorMod(-rect.y(), LINE_HEIGHT)
            : rect.y();
        for (int i = 0; i < lines.size(); i++) {
            final Component line = lines.get(i);
            final int x = rect.x() + this.align.offset(rect.width(), context.textWidth(line));
            context.text(line, x, top + i * LINE_HEIGHT);
        }
    }

    @Override
    protected void plain(final PlainContext context) {
        // Plain dialogs render real components, so keep everything (translations included).
        Component line = this.content;
        if (this.bold || this.color != null) {
            Style defaults = Style.style().decoration(TextDecoration.BOLD, this.bold).build();
            if (this.color != null) {
                defaults = defaults.color(TextColor.color(this.color));
            }
            line = Component.text().style(defaults).append(this.content).build();
        }
        context.text(line);
    }

    /**
     * Semantic text colors taken from the theme.
     */
    public enum Tone {
        NORMAL,
        MUTED,
        ON_ACCENT,
        DISABLED
    }
}
