package me.chubbyduck.canvas.codegen;

public interface GeneratorConstants {

    String BASE_PACKAGE = "me.chubbyduck.canvas";

    static String makePackage(String... parts) {
        return BASE_PACKAGE + "." + String.join(".", parts);
    }

}
