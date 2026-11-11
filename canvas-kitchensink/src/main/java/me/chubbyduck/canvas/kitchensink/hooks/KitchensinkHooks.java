package me.chubbyduck.canvas.kitchensink.hooks;

import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

/**
 * Server actions pages need, kept behind an interface so pages have no Paper imports and render headlessly in tests.
 */
@NullMarked
public interface KitchensinkHooks {

    KitchensinkHooks NONE = new KitchensinkHooks() {
        @Override
        public void askChat(final Audience viewer, final String prompt, final Consumer<String> answer) {
        }

        @Override
        public void openInventory(final Audience viewer) {
        }
    };

    /**
     * Closes the dialog, asks the player to type in chat, then calls back on the player's thread.
     */
    void askChat(Audience viewer, String prompt, Consumer<String> answer);

    /**
     * Opens a chest inventory over the dialog (closes the canvas with reason EXTERNAL).
     */
    void openInventory(Audience viewer);
}
