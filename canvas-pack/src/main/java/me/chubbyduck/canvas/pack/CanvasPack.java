package me.chubbyduck.canvas.pack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.pack.internal.PackAtlas;
import org.jspecify.annotations.NullMarked;

/** A built Canvas resource pack: the glyph atlas the compiler uses, and the zip players download. */
@NullMarked
public class CanvasPack {

    /** Fixed entry timestamp (1980-01-01) so the zip, and its hash, are reproducible. */
    private static final long ZIP_TIME = 315532800000L;

    private final PackAtlas atlas;
    private final Set<Class<?>> registered;
    private final byte[] zip;
    private final String sha1;

    CanvasPack(final PackAtlas atlas, final Map<String, byte[]> files, final Set<Class<?>> registered) {
        this.atlas = atlas;
        this.registered = registered;
        this.zip = zip(files);
        this.sha1 = sha1(this.zip);
    }

    private static byte[] zip(final Map<String, byte[]> files) {
        try (final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             final ZipOutputStream archive = new ZipOutputStream(bytes)) {
            for (final Map.Entry<String, byte[]> file : files.entrySet()) {
                final ZipEntry entry = new ZipEntry(file.getKey());
                entry.setTime(ZIP_TIME);
                archive.putNextEntry(entry);
                archive.write(file.getValue());
                archive.closeEntry();
            }
            archive.finish();
            return bytes.toByteArray();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String sha1(final byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public GlyphAtlas atlas() {
        return this.atlas;
    }

    public byte[] zip() {
        return this.zip.clone();
    }

    public String sha1() {
        return this.sha1;
    }

    public Set<Class<?>> registered() {
        return this.registered;
    }

    public void writeTo(final Path zipFile) throws IOException {
        final Path parent = zipFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(zipFile, this.zip);
    }
}
