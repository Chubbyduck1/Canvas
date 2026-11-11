package me.chubbyduck.canvas.core.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.core.render.PaintedFrame;
import me.chubbyduck.canvas.layout.Rect;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Compiles a {@link PaintedFrame} into a dialog whose body draws the canvas with glyphs. */
@NullMarked
public class GlyphCompiler {

    /** Padding the client's plain-message widget (FocusableTextWidget) puts on each side of the text. */
    public static final int BODY_PADDING = 4;
    private static final int ROW = GlyphAtlas.ROW;
    private static final int TEXT_HEIGHT = 8;
    /** Objects ignore the font; draw them untinted, without shadow or bold. */
    private static final Style OBJECT_STYLE = Style.style()
        .color(NamedTextColor.WHITE)
        .shadowColor(ShadowColor.none())
        .decoration(TextDecoration.BOLD, false)
        .decoration(TextDecoration.ITALIC, false)
        .decoration(TextDecoration.UNDERLINED, false)
        .decoration(TextDecoration.STRIKETHROUGH, false)
        .decoration(TextDecoration.OBFUSCATED, false)
        .build();
    private final GlyphAtlas atlas;

    public GlyphCompiler(final GlyphAtlas atlas) {
        this.atlas = atlas;
    }

    /** Decides which line each piece is drawn on, so the paint order is exact. */
    static void resolveLines(final List<Emit> all, final int lineCount) {
        final List<List<Emit>> byRow = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            byRow.add(new ArrayList<>());
        }
        final boolean[] tallRows = new boolean[lineCount];
        for (final Emit emission : all) {
            emission.line = emission.row;
            for (int row = emission.row; row <= emission.bottomRow; row++) {
                byRow.get(row).add(emission);
            }
            if (emission.bottomRow > emission.row) {
                tallRows[emission.bottomRow] = true;
            }
        }
        for (final List<Emit> bucket : byRow) {
            bucket.sort(Comparator.comparingLong(emission -> emission.order));
        }
        final boolean[] active = tallRows.clone();
        boolean changed = true;
        for (int pass = 0; changed && pass < 2 * lineCount + 2; pass++) {
            changed = false;
            for (int row = 0; row < lineCount; row++) {
                if (!active[row]) {
                    continue;
                }
                final List<Emit> bucket = byRow.get(row);
                for (int j = 1; j < bucket.size(); j++) {
                    final Emit later = bucket.get(j);
                    for (int i = 0; i < j; i++) {
                        final Emit earlier = bucket.get(i);
                        // Objects are drawn at a fixed spot on their line and can't move.
                        if (earlier.object != null || earlier.line <= later.line || !earlier.overlapsX(later)) {
                            continue;
                        }
                        final int verticalOffset = earlier.verticalOffset + ROW * (earlier.row - later.line);
                        if (verticalOffset > GlyphAtlas.MAX_DY) {
                            continue;
                        }
                        earlier.line = later.line;
                        changed = true;
                        // Rows this piece occupies must be re-checked against it.
                        for (int occupied = earlier.row; occupied <= earlier.bottomRow; occupied++) {
                            active[occupied] = true;
                        }
                    }
                }
            }
        }
    }

    static Style glyphStyle(final Key font) {
        return plain(font, NamedTextColor.WHITE);
    }

    /**
     * The style a text run is sent with: its own color and decorations on the offset font, with every decoration it
     * doesn't set turned off explicitly (the dialog would otherwise inherit defaults) and no shadow unless the run asks
     * for one.
     */
    static Style textStyle(final Key font, final Style run) {
        final Style.Builder style = Style.style()
            .font(font)
            .color(run.color() != null ? run.color() : NamedTextColor.WHITE)
            .shadowColor(run.shadowColor() != null ? run.shadowColor() : ShadowColor.none());
        for (final TextDecoration decoration : TextDecoration.values()) {
            style.decoration(decoration, run.decoration(decoration) == TextDecoration.State.TRUE);
        }
        return style.build();
    }

    private static Style plain(final Key font, final TextColor color) {
        return Style.style()
            .font(font)
            .color(color)
            .shadowColor(ShadowColor.none())
            .decoration(TextDecoration.BOLD, false)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.UNDERLINED, false)
            .decoration(TextDecoration.STRIKETHROUGH, false)
            .decoration(TextDecoration.OBFUSCATED, false)
            .build();
    }

    public CompiledDialog compile(
        final PaintedFrame frame,
        final Component title,
        final boolean closeOnEscape,
        final String themeId,
        final String session,
        final int nonce
    ) {
        final NativeDialogParts parts = new NativeDialogParts(session, nonce);
        final int width = frame.width();
        final int lineCount = Math.max(1, Math.ceilDiv(frame.height(), ROW));

        final List<List<Emit>> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            lines.add(new ArrayList<>());
        }
        final List<Emit> all = new ArrayList<>();
        for (final PaintedFrame.DrawOp operation : frame.ops()) {
            switch (operation) {
                case final PaintedFrame.SpriteOp sprite -> this.emitSprite(sprite, themeId, all, lineCount);
                case final PaintedFrame.NineOp nine -> this.emitNine(nine, themeId, all, lineCount);
                case final PaintedFrame.TextOp text -> this.emitText(text, all, lineCount);
                case final PaintedFrame.BarOp bar -> this.emitBar(bar, all, lineCount);
                case final PaintedFrame.ObjectOp object -> this.emitObject(object, all, lineCount);
            }
        }
        resolveLines(all, lineCount);
        for (final Emit emission : all) {
            lines.get(emission.line).add(emission);
        }

        final List<Component> rendered = new ArrayList<>(lineCount);
        final String[] hitPayloads = new String[frame.hits().size()];
        for (int i = 0; i < hitPayloads.length; i++) {
            hitPayloads[i] = parts.handler(frame.hits().get(i).onClick());
        }
        for (int row = 0; row < lineCount; row++) {
            final List<Emit> emits = lines.get(row);
            emits.sort(Comparator.comparingLong(emission -> emission.order));
            rendered.add(this.line(row, width, emits, frame.hits(), hitPayloads));
        }

        for (final var input : frame.inputs()) {
            parts.input(input);
        }
        for (final var footer : frame.footers()) {
            parts.footer(footer);
        }
        final Component body = Component.join(JoinConfiguration.newlines(), rendered);
        // The client draws plain messages in a widget with BODY_PADDING on each side, so its text
        // area is narrower than the requested width. Every line is exactly `width` wide; if the
        // text area were narrower, the client would wrap each line in two.
        final int bodyWidth = Math.min(1024, width + 2 * BODY_PADDING + 1);
        return parts.finish(title, List.of(new RenderedDialog.Body(body, bodyWidth)), 2, closeOnEscape);
    }

    private void emitSprite(final PaintedFrame.SpriteOp operation, final String themeId, final List<Emit> output, final int lineCount) {
        final Key key = operation.sprite().key();
        final int phase = Math.floorMod(operation.y(), ROW);
        final int firstRow = Math.floorDiv(operation.y(), ROW);
        final int bands = this.atlas.spriteBandCount(key, themeId, phase);
        for (int band = 0; band < bands; band++) {
            final int row = firstRow + band;
            if (row < 0 || row >= lineCount) {
                continue;
            }
            final GlyphAtlas.Glyph glyph = this.atlas.spriteBand(key, themeId, phase, band);
            if (glyph != null) {
                output.add(Emit.glyph(operation.x(), row, band == 0 ? phase : 0, operation.order(), glyph));
            }
        }
    }

    private void emitNine(final PaintedFrame.NineOp operation, final String themeId, final List<Emit> output, final int lineCount) {
        final Rect rect = operation.rect();
        final int firstRow = Math.max(0, Math.floorDiv(rect.y(), ROW));
        final int lastRow = Math.min(lineCount - 1, Math.floorDiv(rect.bottom() - 1, ROW));
        for (int row = firstRow; row <= lastRow; row++) {
            final int top = Math.max(rect.y(), row * ROW);
            final int bottom = Math.min(rect.bottom(), row * ROW + ROW);
            if (bottom <= top) {
                continue;
            }
            final GlyphAtlas.NineBand band = this.atlas.nineBand(
                operation.skin().key(), themeId, rect.height(), top - rect.y(), bottom - rect.y());
            final int verticalOffset = top - row * ROW;
            if (band.left() != null) {
                output.add(Emit.glyph(rect.x(), row, verticalOffset, operation.order(), band.left()));
            }
            int x = rect.x() + band.leftWidth();
            int remaining = rect.width() - band.leftWidth() - band.rightWidth();
            for (int bit = band.mids().length - 1; bit >= 0 && remaining > 0; bit--) {
                final int pieceWidth = 1 << bit;
                while (remaining >= pieceWidth) {
                    final GlyphAtlas.Glyph mid = band.mids()[bit];
                    if (mid != null) {
                        output.add(Emit.glyph(x, row, verticalOffset, operation.order(), mid));
                    }
                    x += pieceWidth;
                    remaining -= pieceWidth;
                }
            }
            if (band.right() != null) {
                output.add(Emit.glyph(rect.right() - band.rightWidth(), row, verticalOffset, operation.order(), band.right()));
            }
        }
    }

    private void emitObject(final PaintedFrame.ObjectOp operation, final List<Emit> output, final int lineCount) {
        final int row = operation.row();
        if (row < 0 || row >= lineCount) {
            return;
        }
        final Emit emission = new Emit(operation.x(), row, row, 0, operation.order(), "", StyledText.OBJECT_ADVANCE, true, null);
        emission.object = Component.object(operation.contents()).style(OBJECT_STYLE);
        output.add(emission);
    }

    private void emitBar(final PaintedFrame.BarOp operation, final List<Emit> output, final int lineCount) {
        final int row = Math.floorDiv(operation.y(), ROW);
        if (row < 0 || row >= lineCount) {
            return;
        }
        final int verticalOffset = operation.y() - row * ROW;
        final TextColor tint = TextColor.color(operation.rgb() & 0xFFFFFF);
        int x = operation.x();
        int remaining = operation.width();
        for (int bit = GlyphAtlas.BAR_PIECES - 1; bit >= 0 && remaining > 0; bit--) {
            final int pieceWidth = 1 << bit;
            while (remaining >= pieceWidth) {
                final Emit emission = Emit.glyph(x, row, verticalOffset, operation.order(), this.atlas.bar(bit));
                emission.tint = tint;
                output.add(emission);
                x += pieceWidth;
                remaining -= pieceWidth;
            }
        }
    }

    private void emitText(final PaintedFrame.TextOp operation, final List<Emit> output, final int lineCount) {
        final int row = Math.floorDiv(operation.y(), ROW);
        if (row < 0 || row >= lineCount) {
            return;
        }
        final int verticalOffset = operation.y() - row * ROW;
        int advance = 0;
        for (int i = 0; i < operation.text().length(); i++) {
            advance += this.atlas.textAdvance(operation.text().charAt(i), operation.bold());
        }
        final int bottomRow = Math.min(lineCount - 1, verticalOffset + TEXT_HEIGHT > ROW ? row + 1 : row);
        output.add(Emit.text(operation.x(), row, bottomRow, verticalOffset, operation.order(), operation.text(), operation.style(), advance));
    }

    private Component line(
        final int row,
        final int width,
        final List<Emit> emits,
        final List<PaintedFrame.Hit> hits,
        final String[] payloads
    ) {
        final LineBuilder output = new LineBuilder();
        final Style base = glyphStyle(this.atlas.spriteFont(0));

        // Click section: who owns each pixel column on this row (later hits win).
        final int[] owner = new int[width];
        Arrays.fill(owner, -1);
        final int rowTop = row * ROW;
        final int rowBottom = rowTop + ROW;
        for (int i = 0; i < hits.size(); i++) {
            final Rect rect = hits.get(i).rect();
            if (rect.y() >= rowBottom || rect.bottom() <= rowTop) {
                continue;
            }
            for (int x = Math.max(0, rect.x()); x < Math.min(width, rect.right()); x++) {
                owner[x] = i;
            }
        }
        int start = 0;
        while (start < width) {
            int end = start;
            while (end < width && owner[end] == owner[start]) {
                end++;
            }
            final int id = owner[start];
            final Style style = id < 0 ? base : this.hitStyle(base, hits.get(id), payloads[id]);
            output.append(this.atlas.space(end - start), style);
            start = end;
        }

        // Every line ends at width + 1. A glyph flush with the right edge moves the cursor one
        // pixel past it (its spacing pixel), and the client sizes the body by re-splitting the
        // text at the widest line's width: lines must be that wide, or each one splits in two
        // when measuring (doubling the height and adding a scrollbar).
        final int lineWidth = width + 1;
        if (emits.isEmpty()) {
            output.append(this.atlas.space(lineWidth - width), base);
            return output.build();
        }

        // Visual section.
        output.append(this.atlas.space(-width), base);
        int cursor = 0;
        for (final Emit emission : emits) {
            final int verticalOffset = emission.drawDy();
            final Style style = emission.object != null
                ? base
                : emission.isText
                  ? textStyle(this.atlas.textFont(verticalOffset), Objects.requireNonNull(emission.style))
                  : plain(this.atlas.spriteFont(verticalOffset), emission.tint);
            if (emission.x != cursor) {
                // Bold adds a pixel to every glyph, spaces included, so moves must never be bold.
                final Style move = emission.bold() ? style.decoration(TextDecoration.BOLD, false) : style;
                output.append(this.atlas.space(emission.x - cursor), move);
            }
            if (emission.object != null) {
                output.append(emission.object);
            } else {
                output.append(emission.content, style);
            }
            cursor = emission.x + emission.advance;
        }
        if (cursor != lineWidth) {
            output.append(this.atlas.space(lineWidth - cursor), base);
        }
        return output.build();
    }

    private Style hitStyle(final Style base, final PaintedFrame.Hit hit, final String payload) {
        final Style.Builder style = base.toBuilder()
            .clickEvent(ClickEvent.custom(RenderedDialog.ACTION, BinaryTagHolder.binaryTagHolder(payload)));
        if (hit.tooltip() != null) {
            style.hoverEvent(HoverEvent.showText(hit.tooltip()));
        }
        return style.build();
    }

    /** One glyph or text run on a line. */
    static final class Emit {

        private final int x;
        private final int row;
        private final int bottomRow;
        private final int verticalOffset;
        private final long order;
        private final String content;
        private final int advance;
        private final boolean isText;
        private final @Nullable Style style;
        /** Color a glyph is drawn with; white keeps a sprite's own colors. */
        private TextColor tint = NamedTextColor.WHITE;
        /** An object component to send as is, or {@code null}. */
        @Nullable Component object;
        private int line;

        private Emit(
            final int x,
            final int row,
            final int bottomRow,
            final int verticalOffset,
            final long order,
            final String content,
            final int advance,
            final boolean isText,
            final @Nullable Style style
        ) {
            this.x = x;
            this.row = row;
            this.bottomRow = bottomRow;
            this.verticalOffset = verticalOffset;
            this.order = order;
            this.content = content;
            this.advance = advance;
            this.isText = isText;
            this.style = style;
            this.line = row;
        }

        static Emit glyph(final int x, final int row, final int verticalOffset, final long order, final GlyphAtlas.Glyph glyph) {
            return new Emit(x, row, row, verticalOffset, order, glyph.text(), glyph.advance(), false, null);
        }

        static Emit text(
            final int x,
            final int row,
            final int bottomRow,
            final int verticalOffset,
            final long order,
            final String text,
            final Style style,
            final int advance
        ) {
            return new Emit(x, row, bottomRow, verticalOffset, order, text, advance, true, style);
        }

        boolean bold() {
            return this.style != null && this.style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
        }

        int drawDy() {
            return this.verticalOffset + ROW * (this.row - this.line);
        }

        /** Visible horizontal extent; a glyph's advance includes one pixel of spacing. */
        boolean overlapsX(final Emit other) {
            final int right = this.x + (this.isText ? this.advance : Math.max(1, this.advance - 1));
            final int otherRight = other.x + (other.isText ? other.advance : Math.max(1, other.advance - 1));
            return this.x < otherRight && other.x < right;
        }
    }

    /** Builds a line from styled runs, merging adjacent runs with equal styles. */
    private static final class LineBuilder {

        private final List<Component> parts = new ArrayList<>();
        private final StringBuilder run = new StringBuilder();
        private @Nullable Style style;

        void append(final Component component) {
            this.flush();
            this.style = null;
            this.parts.add(component);
        }

        void append(final String text, final Style runStyle) {
            if (text.isEmpty()) {
                return;
            }
            if (!Objects.equals(this.style, runStyle)) {
                this.flush();
                this.style = runStyle;
            }
            this.run.append(text);
        }

        private void flush() {
            if (!this.run.isEmpty() && this.style != null) {
                this.parts.add(Component.text(this.run.toString(), this.style));
            }
            this.run.setLength(0);
        }

        Component build() {
            this.flush();
            if (this.parts.size() == 1) {
                return this.parts.getFirst();
            }
            final TextComponent.Builder builder = Component.text();
            builder.append(this.parts);
            return builder.build();
        }
    }
}
