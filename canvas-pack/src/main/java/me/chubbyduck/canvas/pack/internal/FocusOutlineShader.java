package me.chubbyduck.canvas.pack.internal;

import java.util.List;
import org.jspecify.annotations.NullMarked;

/**
 * A patched {@code minecraft:core/gui} fragment shader that hides the white outline the client draws around a dialog's
 * text body once it is focused (clicking a canvas focuses it).
 */
@NullMarked
public final class FocusOutlineShader {

    public static final String PATH = "assets/minecraft/shaders/core/gui.fsh";
    private static final String HEADER_NOTE = """
        // Canvas: copy of vanilla core/gui.fsh that also hides the white focus outline dialogs
        // draw around a clicked canvas (exactly opaque white flat fills).
        """;
    private static final String SOURCE_26_3 = "#version 330\n"
        + "#extension GL_ARB_separate_shader_objects : require\n"
        + "\n"
        + HEADER_NOTE
        + "layout(std140) uniform DynamicTransforms {\n"
        + "    mat4 ModelViewMat;\n"
        + "    mat4 TextureMat;\n"
        + "    vec4 ColorModulator;\n"
        + "    vec3 ModelOffset;\n"
        + "};\n"
        + "\n"
        + "layout(location = 0) in vec4 vertexColor;\n"
        + "\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "\n"
        + "void main() {\n"
        + "    vec4 color = vertexColor;\n"
        + "    if (color.a == 0.0) {\n"
        + "        discard;\n"
        + "    }\n"
        + "    if (color == vec4(1.0)) {\n"
        + "        discard;\n"
        + "    }\n"
        + "    fragColor = color * ColorModulator;\n"
        + "}\n";

    public static final List<Variant> VARIANTS = List.of(
        // 1.21.7 - 1.21.8
        new Variant(64, 64, "canvas_focus_64", legacySource("150", true)),
        // 1.21.9 - 1.21.10
        new Variant(69, 69, "canvas_focus_69", legacySource("330", true)),
        // 1.21.11, 26.1 and 26.2 share one gui.fsh
        new Variant(75, 88, "canvas_focus_75", legacySource("330", false)),
        // 26.3: separate shader objects, explicit locations, TextureMat moved up
        new Variant(97, 97, "canvas_focus_97", SOURCE_26_3)
    );

    private FocusOutlineShader() {
    }

    private static String legacySource(final String glslVersion, final boolean lineWidth) {
        return "#version " + glslVersion + "\n"
            + "\n"
            + HEADER_NOTE
            + "layout(std140) uniform DynamicTransforms {\n"
            + "    mat4 ModelViewMat;\n"
            + "    vec4 ColorModulator;\n"
            + "    vec3 ModelOffset;\n"
            + "    mat4 TextureMat;\n"
            + (lineWidth ? "    float LineWidth;\n" : "")
            + "};\n"
            + "\n"
            + "in vec4 vertexColor;\n"
            + "\n"
            + "out vec4 fragColor;\n"
            + "\n"
            + "void main() {\n"
            + "    vec4 color = vertexColor;\n"
            + "    if (color.a == 0.0) {\n"
            + "        discard;\n"
            + "    }\n"
            + "    if (color == vec4(1.0)) {\n"
            + "        discard;\n"
            + "    }\n"
            + "    fragColor = color * ColorModulator;\n"
            + "}\n";
    }

    /**
     * The {@code overlays} section of pack.mcmeta.
     */
    public static String overlaysJson() {
        final StringBuilder out = new StringBuilder("  \"overlays\": {\n    \"entries\": [\n");
        for (int i = 0; i < VARIANTS.size(); i++) {
            final Variant variant = VARIANTS.get(i);
            out.append("      {\"formats\": [").append(variant.minFormat()).append(", ").append(variant.maxFormat())
                .append("], \"min_format\": ").append(variant.minFormat())
                .append(", \"max_format\": ").append(variant.maxFormat())
                .append(", \"directory\": \"").append(variant.directory()).append("\"}")
                .append(i + 1 < VARIANTS.size() ? ",\n" : "\n");
        }
        return out.append("    ]\n  }\n").toString();
    }

    /**
     * One shader variant.
     */
    public record Variant(int minFormat, int maxFormat, String directory, String source) {
    }
}
