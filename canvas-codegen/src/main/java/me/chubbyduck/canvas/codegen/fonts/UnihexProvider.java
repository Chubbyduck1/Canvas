package me.chubbyduck.canvas.codegen.fonts;

import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code "type": "unihex"}: a zip of GNU Unifont {@code .hex} files, with optional width overrides.
 */
public record UnihexProvider(
    @SerializedName("hex_file") @NonNull String hexFile,
    @SerializedName("size_overrides") @Nullable List<SizeOverride> sizeOverrides,
    @Nullable Map<String, Boolean> filter
) implements FontProvider {

    /**
     * Bold draws unihex glyphs half a pixel apart, in half-pixels.
     */
    public static final int BOLD_OFFSET = 1;
    private static final int ROWS = 16;

    /**
     * Codepoint to {@code {advance, boldOffset}} for every glyph in the zip; later {@code .hex} entries win.
     */
    public Map<Integer, int[]> glyphs(final byte[] zip) throws IOException {
        final Map<Integer, int[]> glyphs = new HashMap<>();

        try (final ZipInputStream entries = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = entries.getNextEntry()) != null) {
                if (!entry.getName().endsWith(".hex")) {
                    continue;
                }
                final BufferedReader lines = new BufferedReader(new InputStreamReader(entries, StandardCharsets.UTF_8));
                String line;
                while ((line = lines.readLine()) != null) {
                    final int colon = line.indexOf(':');
                    if (colon < 0) {
                        continue;
                    }
                    final int codepoint = Integer.parseInt(line.substring(0, colon), 16);
                    final int advance = this.advance(codepoint, line.substring(colon + 1).trim());
                    if (advance >= 0) {
                        glyphs.put(codepoint, new int[]{advance, BOLD_OFFSET});
                    }
                }
            }
        }

        return glyphs;
    }

    /**
     * Advance = (right - left + 1) / 2 + 1, from the size override covering the codepoint or else the inked columns of its
     * hex bitmap; -1 if the bitmap is not 8, 16, 24 or 32 pixels wide.
     */
    public int advance(final int codepoint, final String data) {
        if (this.sizeOverrides != null) {
            for (final SizeOverride override : this.sizeOverrides) {
                if (override.contains(codepoint)) {
                    return override.advance();
                }
            }
        }

        final int bitWidth = data.length() * 4 / ROWS;

        if (bitWidth != 8 && bitWidth != 16 && bitWidth != 24 && bitWidth != 32) {
            return -1;
        }

        int mask = 0;
        final int digits = bitWidth / 4;

        for (int row = 0; row < ROWS; row++) {
            final long bits = Long.parseLong(data.substring(row * digits, (row + 1) * digits), 16);
            mask |= (int) (bits << (32 - bitWidth));
        }

        final int left = mask == 0 ? 0 : Integer.numberOfLeadingZeros(mask);
        final int right = mask == 0 ? bitWidth : 31 - Integer.numberOfTrailingZeros(mask);
        return (right - left + 1) / 2 + 1;
    }

    /**
     * Fixes the inked columns {@code [left, right]} of every codepoint from {@code from} to {@code to}.
     */
    public record SizeOverride(@NonNull String from, @NonNull String to, int left, int right) {

        public boolean contains(final int codepoint) {
            return codepoint >= this.from.codePointAt(0) && codepoint <= this.to.codePointAt(0);
        }

        public int advance() {
            return (this.right - this.left + 1) / 2 + 1;
        }

    }
}
