package me.chubbyduck.canvas.codegen.unicode;

import java.util.Map;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;

/**
 * {@code ArabicShaping.txt}: the explicitly listed joining type ({@code R, L, D, C, U, T}) of each codepoint.
 *
 * @param version the Unicode version named in the file header, e.g. {@code 16.0.0}
 */
public record ArabicShapingFile(@NonNull String version, @NonNull Map<Integer, Character> joiningTypes) {

    private static final String HEADER = "# ArabicShaping-";

    /**
     * Lines look like {@code 0644; LAM; D; LAM}: codepoint, name, joining type, joining group.
     */
    @NonNull
    public static ArabicShapingFile parse(final @NonNull String text) {
        String version = "unknown";
        final Map<Integer, Character> joiningTypes = new TreeMap<>();

        for (final String line : text.split("\n")) {
            if (line.startsWith(HEADER)) {
                version = line.substring(HEADER.length(), line.indexOf(".txt"));
                continue;
            }

            final String data = line.contains("#") ? line.substring(0, line.indexOf('#')) : line;
            if (data.isBlank()) {
                continue;
            }

            final String[] fields = data.split(";");
            joiningTypes.put(Integer.parseInt(fields[0].trim(), 16), fields[2].trim().charAt(0));
        }

        return new ArabicShapingFile(version, joiningTypes);
    }

}
