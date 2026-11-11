package me.chubbyduck.canvas.kitchensink.node;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.elements.skin.PixelCanvas;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

/**
 * 11x8 pixel-art flags for the language dropdown.
 */
@NullMarked
public class Flags extends Node {

    private static final int WIDTH = 11;
    private static final int HEIGHT = 8;
    private static final int RED = 0xFFD52B1E;
    private static final int WHITE = 0xFFF4F4F4;
    private static final int BLUE = 0xFF1E3A8A;
    private static final int NAVY = 0xFF0A2463;
    private static final Map<Language, Sprite> SPRITES = new EnumMap<>(Language.class);

    private Flags() {
    }

    public static Sprite of(final Language language) {
        return Objects.requireNonNull(SPRITES.get(language), "Flags.class is not registered at bootstrap");
    }

    private static PixelCanvas paint(final Language language) {
        final PixelCanvas pixels = PixelCanvas.canvas(WIDTH, HEIGHT);
        switch (language) {
            case AUTO -> {
                pixels.shape(0, 0, 0xFF2F80D1,
                    "...#####...",
                    "..#######..",
                    ".#########.",
                    ".#########.",
                    ".#########.",
                    ".#########.",
                    "..#######..",
                    "...#####...");
                pixels.shape(0, 0, 0xFF3FA34D,
                    "...........",
                    "...##......",
                    "..####..#..",
                    "...###.###.",
                    "....#..##..",
                    "......##...",
                    "...........",
                    "...........");
            }
            case EN_US -> {
                for (int y = 0; y < HEIGHT; y++) {
                    pixels.fill(0, y, WIDTH, 1, y % 2 == 0 ? RED : WHITE);
                }
                pixels.fill(0, 0, 5, 4, NAVY).set(1, 1, WHITE).set(3, 1, WHITE).set(2, 2, WHITE);
            }
            case EN_CA -> {
                pixels.fill(WHITE).fill(0, 0, 3, HEIGHT, RED).fill(8, 0, 3, HEIGHT, RED);
                pixels.shape(3, 2, RED, "..#..", ".###.", "#####", "..#..");
            }
            case EN_UK -> {
                pixels.fill(BLUE);
                pixels.shape(0, 0, WHITE,
                    "##.......##",
                    ".##.....##.",
                    "..##...##..",
                    "...##.##...",
                    "...##.##...",
                    "..##...##..",
                    ".##.....##.",
                    "##.......##");
                pixels.fill(0, 3, WIDTH, 2, WHITE).fill(4, 0, 3, HEIGHT, WHITE);
                pixels.fill(0, 3, WIDTH, 2, RED).fill(5, 0, 1, HEIGHT, RED);
            }
            case EN_AU, EN_NZ -> {
                pixels.fill(NAVY);
                pixels.fill(0, 0, 5, 4, BLUE).fill(0, 1, 5, 1, WHITE).fill(2, 0, 1, 4, WHITE).set(2, 1, RED);
                final int star = language == Language.EN_AU ? WHITE : RED;
                pixels.set(8, 1, star).set(7, 3, star).set(9, 4, star).set(8, 6, star).set(2, 6, WHITE);
            }
            case PL -> pixels.fill(0, 0, WIDTH, HEIGHT / 2, WHITE).fill(0, HEIGHT / 2, WIDTH, HEIGHT / 2, RED);
        }
        return pixels.outline(0x66000000);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        for (final Language language : Language.values()) {
            final Key key = Key.key("kitchensink", "flags/" + language.name().toLowerCase(Locale.ROOT));
            SPRITES.put(language, assets.registerSprite(key, theme -> paint(language).image()));
        }
    }

    /**
     * The languages in the kitchensink's language dropdown.
     */
    public enum Language {
        AUTO("Automatyczny"),
        EN_US("English (US)"),
        EN_CA("English (CA)"),
        EN_UK("English (UK)"),
        EN_AU("English (AU)"),
        EN_NZ("English (NZ)"),
        PL("Polski (PL)");

        private final String display;

        Language(final String display) {
            this.display = display;
        }

        public String display() {
            return this.display;
        }
    }
}
