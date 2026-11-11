package me.chubbyduck.canvas.codegen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import me.chubbyduck.canvas.codegen.generators.ArabicShapingGenerator;
import me.chubbyduck.canvas.codegen.generators.FontWidthGenerator;
import me.chubbyduck.canvas.codegen.generators.VanillaFontMetricsGenerator;

public class CodeGenerators {

    static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }

        final Path path = Path.of(args[0]);
        final String version = "latest";

        final List<FontWidthGenerator> fontGenerators = List.of(
            new FontWidthGenerator("Default", "default", "latest"),
            new FontWidthGenerator("Alt", "alt", "latest")
        );

        final List<Generator> generators = new ArrayList<>(fontGenerators);
        generators.add(new VanillaFontMetricsGenerator(fontGenerators));
        generators.add(new ArabicShapingGenerator("latest"));

        for (final Generator generator : generators) {
            generator.generateTo(path);
        }
    }

}
