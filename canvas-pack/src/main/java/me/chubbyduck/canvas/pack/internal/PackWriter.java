package me.chubbyduck.canvas.pack.internal;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.pack.PackOptions;
import org.jspecify.annotations.NullMarked;

/**
 * Turns glyph sheets into resource pack files: textures, fonts, pack.mcmeta and shader overlays.
 */
@NullMarked
public final class PackWriter {

    /**
     * The vanilla default font's ASCII sheet, reused (not copied) by the text fonts.
     */
    private static final String VANILLA_ASCII = "minecraft:font/ascii.png";
    private static final String EMPTY_ROW = "\u0000".repeat(16);
    private static final List<String> ASCII_ROWS = List.of(
        EMPTY_ROW,
        EMPTY_ROW,
        " !\"#$%&'()*+,-./",
        "0123456789:;<=>?",
        "@ABCDEFGHIJKLMNO",
        "PQRSTUVWXYZ[\\]^_",
        "`abcdefghijklmno",
        "pqrstuvwxyz{|}~\u0000",
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW,
        EMPTY_ROW
    );
    /**
     * Minecraft draws a bitmap glyph's top at {@code lineTop + BASELINE - ascent}.
     */
    private static final int BASELINE = 7;
    private static final int TEXT_HEIGHT = 8;

    private PackWriter() {
    }

    public static Map<String, byte[]> write(final PackOptions options, final List<GlyphTable.Sheet> sheets) {
        final Map<String, byte[]> files = new TreeMap<>();
        files.put("pack.mcmeta", utf8(packMeta(options)));
        for (final GlyphTable.Sheet sheet : sheets) {
            files.put("assets/" + PackAtlas.NAMESPACE + "/textures/" + sheet.path(), ImageTools.png(sheet.image()));
        }

        final Map<String, Number> spaces = spaceAdvances();
        // Obfuscation chooses random glyphs from the whole font by advance. Keep cursor moves
        // and unshifted fallback glyphs out of this pool; the compiler emits those in other fonts.
        final Map<String, Number> textSpaces = Map.of(" ", 4);
        for (int dy = 0; dy <= GlyphAtlas.MAX_DY; dy++) {
            final StringBuilder sprites = new StringBuilder("{\"providers\":[").append(space(spaces));
            for (final GlyphTable.Sheet sheet : sheets) {
                sprites.append(',').append(bitmap(PackAtlas.NAMESPACE + ":" + sheet.path(), GlyphAtlas.ROW, BASELINE - dy, sheet.chars()));
            }
            files.put(font("r" + dy), utf8(sprites.append("]}").toString()));
            files.put(font("t" + dy), utf8("{\"providers\":[" + space(textSpaces) + ","
                + bitmap(VANILLA_ASCII, TEXT_HEIGHT, BASELINE - dy, ASCII_ROWS) + "]}"));
        }

        if (!options.hideFocusOutline()) {
            return files;
        }
        for (final FocusOutlineShader.Variant variant : FocusOutlineShader.VARIANTS) {
            files.put(variant.directory() + "/" + FocusOutlineShader.PATH, utf8(variant.source()));
        }
        return files;
    }

    private static String font(final String name) {
        return "assets/" + PackAtlas.NAMESPACE + "/font/" + name + ".json";
    }

    private static String space(final Map<String, Number> advances) {
        final StringBuilder out = new StringBuilder("{\"type\":\"space\",\"advances\":{");
        boolean first = true;
        for (final Map.Entry<String, Number> advance : advances.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append(quote(advance.getKey())).append(':').append(advance.getValue());
        }
        return out.append("}}").toString();
    }

    private static String bitmap(final String file, final int height, final int ascent, final List<String> rows) {
        final StringBuilder out = new StringBuilder("{\"type\":\"bitmap\",\"file\":").append(quote(file))
            .append(",\"height\":").append(height)
            .append(",\"ascent\":").append(ascent)
            .append(",\"chars\":[");
        for (int i = 0; i < rows.size(); i++) {
            out.append(i == 0 ? "" : ",").append(quote(rows.get(i)));
        }
        return out.append("]}").toString();
    }

    /**
     * A JSON string literal; control characters are escaped, everything else is written as UTF-8.
     */
    private static String quote(final String text) {
        final StringBuilder out = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (character == '"' || character == '\\') {
                out.append('\\').append(character);
                continue;
            }
            if (character < 0x20) {
                out.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
                continue;
            }
            out.append(character);
        }
        return out.append('"').toString();
    }

    private static byte[] utf8(final String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, Number> spaceAdvances() {
        final Map<String, Number> spaces = new LinkedHashMap<>();
        for (int bit = 0; bit <= PackAtlas.MAX_SPACE_BIT; bit++) {
            spaces.put(Character.toString(PackAtlas.spaceCodepoint(bit, false)), 1 << bit);
            spaces.put(Character.toString(PackAtlas.spaceCodepoint(bit, true)), -(1 << bit));
        }
        spaces.put(Character.toString(PackAtlas.HALF_PIXEL_SPACE), 0.5);
        for (final int control : new int[]{0x2066, 0x2068, 0x2069}) {
            spaces.put(Character.toString(control), 0);
        }
        return spaces;
    }

    /**
     * {@code pack_format}/{@code supported_formats} for 1.21.6-1.21.8 clients and {@code min_format}/{@code max_format}
     * for 1.21.9+.
     */
    private static String packMeta(final PackOptions options) {
        final String description = options.description().replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\n"
            + "  \"pack\": {\n"
            + "    \"description\": \"" + description + "\",\n"
            + "    \"pack_format\": " + options.maxFormat() + ",\n"
            + "    \"supported_formats\": [" + options.minFormat() + ", " + options.maxFormat() + "],\n"
            + "    \"min_format\": " + options.minFormat() + ",\n"
            + "    \"max_format\": " + options.maxFormat() + "\n"
            + (options.hideFocusOutline() ? "  },\n" + FocusOutlineShader.overlaysJson() : "  }\n")
            + "}\n";
    }
}
