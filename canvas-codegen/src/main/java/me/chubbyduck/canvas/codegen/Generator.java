package me.chubbyduck.canvas.codegen;

import java.io.IOException;
import java.nio.file.Path;

public interface Generator {

    void generateTo(Path output) throws IOException;

}
