package me.chubbyduck.canvas.paper.platform;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Reads the top-level scalar fields of an SNBT compound, e.g. {@code {s:"abc",v:3,a:'h0'}}. */
@NullMarked
public final class SnbtParser {

    private final String text;
    private int position;

    private SnbtParser(final String text) {
        this.text = text;
    }

    public static @Nullable Map<String, String> fields(final String snbt) {
        final SnbtParser reader = new SnbtParser(snbt.trim());
        try {
            return reader.compound();
        } catch (final IllegalArgumentException | IndexOutOfBoundsException exception) {
            return null;
        }
    }

    private static String stripSuffix(final String value) {
        if (value.length() < 2) {
            return value;
        }
        final char preceding = value.charAt(value.length() - 2);
        if ("bBsSlLfFdD".indexOf(value.charAt(value.length() - 1)) >= 0
            && (Character.isDigit(preceding) || preceding == '.')) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private Map<String, String> compound() {
        this.expect('{');
        final Map<String, String> fields = new HashMap<>();
        this.skipSpace();
        if (this.peek() == '}') {
            this.position++;
            return fields;
        }
        while (true) {
            this.skipSpace();
            final String key = this.peek() == '"' || this.peek() == '\'' ? this.quoted() : this.bare();
            this.skipSpace();
            this.expect(':');
            this.skipSpace();
            final char character = this.peek();
            if (character == '{' || character == '[') {
                this.skipNested();
            } else if (character == '"' || character == '\'') {
                fields.put(key, this.quoted());
            } else {
                fields.put(key, stripSuffix(this.bare()));
            }
            this.skipSpace();
            final char next = this.text.charAt(this.position++);
            if (next == '}') {
                return fields;
            }
            if (next != ',') {
                throw new IllegalArgumentException("Expected , or } at " + (this.position - 1));
            }
        }
    }

    private String quoted() {
        final char quote = this.text.charAt(this.position++);
        final StringBuilder out = new StringBuilder();
        while (true) {
            final char character = this.text.charAt(this.position++);
            if (character == quote) {
                return out.toString();
            }
            if (character == '\\') {
                out.append(this.text.charAt(this.position++));
                continue;
            }
            out.append(character);
        }
    }

    private String bare() {
        final int start = this.position;
        while (this.position < this.text.length() && ",:}] \t\r\n".indexOf(this.text.charAt(this.position)) < 0) {
            this.position++;
        }
        return this.text.substring(start, this.position);
    }

    private void skipNested() {
        int depth = 0;
        do {
            final char character = this.text.charAt(this.position);
            if (character == '"' || character == '\'') {
                this.quoted();
                continue;
            }
            if (character == '{' || character == '[') {
                depth++;
            } else if (character == '}' || character == ']') {
                depth--;
            }
            this.position++;
        } while (depth > 0);
    }

    private void skipSpace() {
        while (this.position < this.text.length() && Character.isWhitespace(this.text.charAt(this.position))) {
            this.position++;
        }
    }

    private char peek() {
        return this.text.charAt(this.position);
    }

    private void expect(final char character) {
        if (this.text.charAt(this.position++) != character) {
            throw new IllegalArgumentException("Expected " + character + " at " + (this.position - 1));
        }
    }
}
