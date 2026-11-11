package me.chubbyduck.canvas.paper;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import java.util.Map;
import java.util.UUID;
import me.chubbyduck.canvas.core.dialog.InputBinding;
import me.chubbyduck.canvas.core.dialog.RenderedDialog;
import me.chubbyduck.canvas.paper.platform.SnbtParser;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Routes dialog clicks, tracks pack status, and cleans up sessions. */
@NullMarked
final class CanvasListener implements Listener {

    private final CanvasRuntime runtime;

    CanvasListener(final CanvasRuntime runtime) {
        this.runtime = runtime;
    }

    @EventHandler
    public void onClick(final PlayerCustomClickEvent event) {
        if (!event.getIdentifier().equals(RenderedDialog.ACTION)) {
            return;
        }
        if (!(event.getCommonConnection() instanceof final PlayerGameConnection connection)) {
            return;
        }
        final Player player = connection.getPlayer();
        final DialogResponseView response = event.getDialogResponseView();
        final BinaryTagHolder holder = response != null ? response.payload() : event.getTag();
        final Map<String, String> payload = holder == null ? null : SnbtParser.fields(holder.string());
        if (payload == null) {
            return;
        }
        final String session = payload.get(RenderedDialog.FIELD_SESSION);
        final String action = payload.get(RenderedDialog.FIELD_ACTION);
        final int nonce;
        try {
            nonce = Integer.parseInt(payload.getOrDefault(RenderedDialog.FIELD_NONCE, ""));
        } catch (final NumberFormatException exception) {
            return;
        }
        if (session == null || action == null) {
            return;
        }
        final InputBinding.InputReader inputs = response == null ? null : new InputBinding.InputReader() {
            @Override
            public @Nullable String text(final String key) {
                return response.getText(key);
            }

            @Override
            public @Nullable Boolean bool(final String key) {
                return response.getBoolean(key);
            }

            @Override
            public @Nullable Float number(final String key) {
                return response.getFloat(key);
            }
        };
        this.runtime.sessions().handleClick(player.getUniqueId(), session, nonce, action, inputs);
    }

    @EventHandler
    public void onPackStatus(final PlayerResourcePackStatusEvent event) {
        // Canvas doesn't send the pack itself, so any loaded pack is taken to include it.
        if (!this.runtime.platform().requiresPack() || event.getStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            return;
        }
        final Player player = event.getPlayer();
        this.runtime.platform().packLoaded(player.getUniqueId());
        this.runtime.rerender(player);
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent event) {
        if (event.getPlugin() != this.runtime.plugin()) {
            return;
        }
        this.runtime.stop();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(final InventoryOpenEvent event) {
        this.runtime.sessions().external(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID id = event.getPlayer().getUniqueId();
        this.runtime.sessions().quit(id);
        this.runtime.platform().forget(id);
    }
}
