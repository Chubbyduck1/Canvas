package me.chubbyduck.canvas.codegen.unicode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code UnicodeData.txt}: one entry per listed codepoint, in ascending order.
 */
public record UnicodeDataFile(@NonNull List<Entry> characters) {

    /**
     * Fields are separated by {@code ;}: 0 = codepoint, 2 = general category, 5 = decomposition such as
     * {@code <final> 0644 0622}.
     */
    @NonNull
    public static UnicodeDataFile parse(final @NonNull String text) {
        final List<Entry> characters = new ArrayList<>();

        for (final String line : text.split("\n")) {
            if (line.isBlank()) {
                continue;
            }

            final String[] fields = line.split(";", -1);
            final String decomposition = fields[5].trim();
            String tag = null;
            String mapping = decomposition;
            if (decomposition.startsWith("<")) {
                tag = decomposition.substring(1, decomposition.indexOf('>'));
                mapping = decomposition.substring(decomposition.indexOf('>') + 1).trim();
            }

            final int[] codepoints = mapping.isEmpty()
                ? new int[0]
                : Arrays.stream(mapping.split(" ")).mapToInt(hex -> Integer.parseInt(hex, 16)).toArray();
            characters.add(new Entry(Integer.parseInt(fields[0], 16), fields[2], tag, codepoints));
        }

        return new UnicodeDataFile(characters);
    }

    /**
     * One codepoint's general category and decomposition.
     *
     * @param decompositionTag the compatibility tag without brackets ({@code isolated}, {@code final}, ...), or
     *                         {@code null} for a canonical or missing decomposition
     */
    @SuppressWarnings("ArrayRecordComponent") // never compared or hashed
    public record Entry(
        int codepoint,
        @NonNull String category,
        @Nullable String decompositionTag,
        int @NonNull [] decomposition
    ) {

        /**
         * Nonspacing marks, enclosing marks and format characters, which Arabic shaping skips over.
         */
        public boolean isTransparent() {
            return this.category.equals("Mn") || this.category.equals("Me") || this.category.equals("Cf");
        }

    }

}
