package me.chubbyduck.canvas.core.atlas;

import java.util.HashMap;
import java.util.Map;
import me.chubbyduck.canvas.text.ArabicShapingData;
import org.jspecify.annotations.NullMarked;

/**
 * Replaces Arabic-script letters with their positional presentation forms (isolated, initial, medial, final) and merges
 * lam + alef into its ligature, so text is measured and sent as the client draws it.
 */
@NullMarked
public final class ArabicShaper {

    private static final int ISOLATED = 0;
    private static final int FINAL = 1;
    private static final int INITIAL = 2;
    private static final int MEDIAL = 3;
    private static final int LAM = 0x0644;

    private static final int[] JOINING_TYPES = ArabicShapingData.joiningTypes();
    /**
     * Base letter to {@code {isolated, final, initial, medial}}, 0 where missing.
     */
    private static final Map<Integer, int[]> FORMS = rows(ArabicShapingData.forms(), 5);
    /**
     * Alef to {@code {isolated, final}} of the lam-alef ligature.
     */
    private static final Map<Integer, int[]> LAM_ALEF = rows(ArabicShapingData.lamAlef(), 3);

    private ArabicShaper() {
    }

    private static Map<Integer, int[]> rows(final int[] table, final int width) {
        final Map<Integer, int[]> rows = new HashMap<>();
        for (int i = 0; i < table.length; i += width) {
            final int[] values = new int[width - 1];
            System.arraycopy(table, i + 1, values, 0, width - 1);
            rows.put(table[i], values);
        }
        return Map.copyOf(rows);
    }

    /**
     * Text with every shapeable letter replaced by the form its neighbors call for; other text is returned as is.
     */
    public static String shape(final String text) {
        if (text.codePoints().noneMatch(FORMS::containsKey)) {
            return text;
        }
        final int[] codepoints = text.codePoints().toArray();
        final StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < codepoints.length; i++) {
            final int codepoint = codepoints[i];
            final int[] ligature = codepoint == LAM && i + 1 < codepoints.length ? LAM_ALEF.get(codepoints[i + 1]) : null;
            if (ligature != null) {
                out.appendCodePoint(joinsPrevious(codepoints, i) ? ligature[FINAL] : ligature[ISOLATED]);
                i++;
                continue;
            }
            final int[] forms = FORMS.get(codepoint);
            if (forms == null) {
                out.appendCodePoint(codepoint);
                continue;
            }
            final boolean previous = joinsPrevious(codepoints, i);
            final boolean next = joinsNext(codepoints, i);
            final int form = previous && next ? MEDIAL : previous ? FINAL : next ? INITIAL : ISOLATED;
            out.appendCodePoint(pick(forms, form, codepoint));
        }
        return out.toString();
    }

    /**
     * The wanted form, falling back to the nearest one that exists (medial to final, initial to isolated).
     */
    private static int pick(final int[] forms, final int form, final int codepoint) {
        int shaped = forms[form];
        if (shaped == 0 && form == MEDIAL) {
            shaped = forms[FINAL];
        }
        if (shaped == 0) {
            shaped = forms[ISOLATED];
        }
        return shaped == 0 ? codepoint : shaped;
    }

    /**
     * Whether the letter at {@code index} connects to the nearest non-transparent letter before it.
     */
    private static boolean joinsPrevious(final int[] codepoints, final int index) {
        if (!joinsBackward(joiningType(codepoints[index]))) {
            return false;
        }
        for (int i = index - 1; i >= 0; i--) {
            final char type = joiningType(codepoints[i]);
            if (type != 'T') {
                return joinsForward(type);
            }
        }
        return false;
    }

    /**
     * Whether the letter at {@code index} connects to the nearest non-transparent letter after it.
     */
    private static boolean joinsNext(final int[] codepoints, final int index) {
        if (!joinsForward(joiningType(codepoints[index]))) {
            return false;
        }
        for (int i = index + 1; i < codepoints.length; i++) {
            final char type = joiningType(codepoints[i]);
            if (type != 'T') {
                return joinsBackward(type);
            }
        }
        return false;
    }

    /**
     * Dual-joining, left-joining, and join-causing letters to connect to the letter after them.
     */
    private static boolean joinsForward(final char type) {
        return type == 'D' || type == 'L' || type == 'C';
    }

    /**
     * Dual-joining, right-joining, and join-causing letters to connect to the letter before them.
     */
    private static boolean joinsBackward(final char type) {
        return type == 'D' || type == 'R' || type == 'C';
    }

    private static char joiningType(final int codepoint) {
        int low = 0;
        int high = JOINING_TYPES.length / 3 - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int offset = mid * 3;
            if (codepoint < JOINING_TYPES[offset]) {
                high = mid - 1;
            } else if (codepoint > JOINING_TYPES[offset + 1]) {
                low = mid + 1;
            } else {
                return (char) JOINING_TYPES[offset + 2];
            }
        }
        return 'U';
    }
}
