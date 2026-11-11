package me.chubbyduck.canvas.elements;

import java.util.List;
import me.chubbyduck.canvas.asset.Theme;
import org.jspecify.annotations.NullMarked;

/** The built-in OreUI themes. */
@NullMarked
public final class Themes {

    /** Light grey cards and buttons, dark text, green selection. */
    public static final Theme ORE = new Theme(
        "ore",
        new Theme.Palette(
            0xFFCECFD1,
            0xFFCECFD1,
            0xFF6F7073,
            0xFFE0E0E2,
            0xFF1E1E1F,
            0xFF000000,
            0xFFE0E0E2,
            0xFFEBEBEC,
            0xFFAFB0B3,
            0xFF8A8B8E,
            0xFF575759,
            0xFF3B8327,
            0xFF629B51,
            0xFF4F8F3C,
            0xFF1D4C13,
            0xFF1E1E1F,
            0xFF67746B,
            0xFFFCFCFC,
            0xFFB1B2B5,
            0xFF7E7E80
        ),
        new Theme.Metrics(1, 1, 20, 20, 6)
    );

    /** The same structure with dark surfaces and light text. */
    public static final Theme ORE_DARK = new Theme(
        "ore_dark",
        new Theme.Palette(
            0xFF313233,
            0xFF48494A,
            0xFF1E1E1F,
            0xFF3C3D3E,
            0xFF000000,
            0xFF000000,
            0xFF5A5B5C,
            0xFF6D6E70,
            0xFF242526,
            0xFF1B1C1D,
            0xFF111112,
            0xFF3B8327,
            0xFF629B51,
            0xFF4F8F3C,
            0xFF1D4C13,
            0xFFFCFCFC,
            0xFFB1B2B5,
            0xFFFCFCFC,
            0xFF3A3B3C,
            0xFF8C8D90
        ),
        new Theme.Metrics(1, 1, 20, 20, 6)
    );

    private Themes() {
    }

    public static List<Theme> all() {
        return List.of(ORE, ORE_DARK);
    }
}
