package me.chubbyduck.canvas.core.atlas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Measures, wraps and splits Adventure components for drawing with the Canvas text fonts.
 */
@NullMarked
public final class StyledText {

    /**
     * Advance of an object component (sprite or player head).
     */
    public static final int OBJECT_ADVANCE = 8;

    /**
     * Stands in for an object in a run's text.
     */
    public static final char OBJECT_CHAR = '￼';

    private StyledText() {
    }

    /**
     * Flattens a component into styled runs, inheriting styles from parents.
     */
    public static List<Run> runs(final Component component) {
        final List<Run> runs = new ArrayList<>();
        final Deque<Style> stack = new ArrayDeque<>();
        stack.push(Style.empty());
        final @Nullable ObjectContents[] pending = new @Nullable ObjectContents[1];
        final ComponentFlattener flattener = ComponentFlattener.basic().toBuilder()
            .mapper(ObjectComponent.class, object -> {
                pending[0] = object.contents();
                return String.valueOf(OBJECT_CHAR);
            })
            .build();
        flattener.flatten(component, new FlattenerListener() {
            @Override
            public void pushStyle(final Style style) {
                stack.push(style.merge(stack.getFirst(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
            }

            @Override
            public void component(final String text) {
                final ObjectContents object = pending[0];
                if (object != null) {
                    runs.add(new Run(String.valueOf(OBJECT_CHAR), stack.getFirst(), object));
                    pending[0] = null;
                    return;
                }
                if (text.isEmpty()) {
                    return;
                }
                runs.add(new Run(clean(text), stack.getFirst()));
            }

            @Override
            public void popStyle(final Style style) {
                stack.pop();
            }
        });
        return runs;
    }

    public static boolean hasObjects(final Component component) {
        if (component instanceof ObjectComponent) {
            return true;
        }
        for (final Component child : component.children()) {
            if (hasObjects(child)) {
                return true;
            }
        }
        return false;
    }

    private static String clean(final String text) {
        return ArabicShaper.shape(text.replace('\t', ' '));
    }

    public static String plain(final Component component) {
        final StringBuilder out = new StringBuilder();
        for (final Run run : runs(component)) {
            out.append(run.text());
        }
        return out.toString();
    }

    public static int width(final Component component) {
        int width = 0;
        for (final Run run : runs(component)) {
            width += width(run);
        }
        return width;
    }

    public static int width(final Run run) {
        if (run.isObject()) {
            return OBJECT_ADVANCE;
        }
        int width = 0;
        for (int i = 0; i < run.text().length(); ) {
            final int character = run.text().codePointAt(i);
            i += Character.charCount(character);
            if (character != '\n') {
                width += TextMetrics.advance(run.style().font(), character, run.bold());
            }
        }
        return width;
    }

    /**
     * Breaks a component into lines: at every newline, and at spaces so no line is wider than {@code maxWidth} (a
     * single word wider than that stays on its own line).
     */
    public static List<Component> wrap(final Component component, final int maxWidth) {
        final List<Component> lines = new ArrayList<>();
        final List<Cell> paragraph = new ArrayList<>();
        for (final Run run : runs(component)) {
            if (run.isObject()) {
                paragraph.add(new Cell(OBJECT_CHAR, run.style(), OBJECT_ADVANCE, run.object()));
                continue;
            }
            for (int i = 0; i < run.text().length(); ) {
                final int character = run.text().codePointAt(i);
                i += Character.charCount(character);
                if (character == '\n') {
                    wrapParagraph(paragraph, maxWidth, lines);
                    paragraph.clear();
                    continue;
                }
                paragraph.add(new Cell(character, run.style(), TextMetrics.advance(run.style().font(), character, run.bold()), null));
            }
        }
        wrapParagraph(paragraph, maxWidth, lines);
        return lines;
    }

    /**
     * The longest prefix of a single line that fits in {@code maxWidth}.
     */
    public static Component fit(final Component component, final int maxWidth) {
        final List<Cell> cells = new ArrayList<>();
        int width = 0;
        for (final Run run : runs(component)) {
            if (run.isObject()) {
                if (width + OBJECT_ADVANCE > maxWidth) {
                    return join(cells);
                }
                width += OBJECT_ADVANCE;
                cells.add(new Cell(OBJECT_CHAR, run.style(), OBJECT_ADVANCE, run.object()));
                continue;
            }
            for (int i = 0; i < run.text().length(); ) {
                final int character = run.text().codePointAt(i);
                i += Character.charCount(character);
                if (character == '\n') {
                    continue;
                }
                final int advance = TextMetrics.advance(run.style().font(), character, run.bold());
                if (width + advance > maxWidth) {
                    return join(cells);
                }
                width += advance;
                cells.add(new Cell(character, run.style(), advance, null));
            }
        }
        return join(cells);
    }

    private static void wrapParagraph(final List<Cell> cells, final int maxWidth, final List<Component> lines) {
        if (maxWidth == Integer.MAX_VALUE || widthOf(cells, 0, cells.size()) <= maxWidth) {
            lines.add(join(cells));
            return;
        }
        List<Cell> line = new ArrayList<>();
        int lineWidth = 0;
        int i = 0;
        while (i < cells.size()) {
            // A token is a run of spaces followed by a word.
            int start = i;
            while (i < cells.size() && cells.get(i).isSpace()) {
                i++;
            }
            final int wordStart = i;
            while (i < cells.size() && !cells.get(i).isSpace()) {
                i++;
            }
            final int spaces = widthOf(cells, start, wordStart);
            final int word = widthOf(cells, wordStart, i);
            if (!line.isEmpty() && lineWidth + spaces + word > maxWidth) {
                lines.add(join(line));
                line = new ArrayList<>();
                lineWidth = 0;
                start = wordStart;
            }
            line.addAll(cells.subList(start, i));
            lineWidth += widthOf(cells, start, i);
        }
        lines.add(join(line));
    }

    private static int widthOf(final List<Cell> cells, final int from, final int to) {
        int width = 0;
        for (int i = from; i < to; i++) {
            width += cells.get(i).advance;
        }
        return width;
    }

    /**
     * Rebuilds a component from cells, merging neighbors with the same style.
     */
    private static Component join(final List<Cell> cells) {
        final TextComponent.Builder out = Component.text();
        final StringBuilder run = new StringBuilder();
        Style style = null;
        for (final Cell cell : cells) {
            if (cell.object != null) {
                if (style != null && !run.isEmpty()) {
                    out.append(Component.text(run.toString(), style));
                }
                run.setLength(0);
                style = null;
                out.append(Component.object(cell.object).style(cell.style));
                continue;
            }
            if (style != null && !style.equals(cell.style)) {
                out.append(Component.text(run.toString(), style));
                run.setLength(0);
            }
            style = cell.style;
            run.appendCodePoint(cell.character);
        }
        if (style != null && !run.isEmpty()) {
            out.append(Component.text(run.toString(), style));
        }
        return out.build();
    }

    /**
     * A piece of text with one style, or a single object.
     */
    public record Run(String text, Style style, @Nullable ObjectContents object) {

        public Run(final String text, final Style style) {
            this(text, style, null);
        }

        public boolean bold() {
            return this.style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
        }

        public boolean isObject() {
            return this.object != null;
        }
    }

    /**
     * One character (or object) with its style and advance.
     */
    private record Cell(int character, Style style, int advance, @Nullable ObjectContents object) {

        boolean isSpace() {
            return this.object == null && this.character == ' ';
        }
    }
}
