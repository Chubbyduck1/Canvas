package me.chubbyduck.canvas.paper.platform;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.chubbyduck.canvas.core.dialog.RenderedDialog;
import me.chubbyduck.canvas.core.session.SessionPlatform;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

/**
 * Paper implementation of the session platform: shows dialogs, schedules on the player's thread (works on Folia too)
 * and decides between the glyph canvas and the plain fallback.
 */
@NullMarked
public final class PaperPlatform implements SessionPlatform {

    private final Plugin plugin;
    private final Set<UUID> packLoaded = ConcurrentHashMap.newKeySet();
    private final Set<UUID> forcedPlain = ConcurrentHashMap.newKeySet();

    private final boolean requirePack;

    public PaperPlatform(final Plugin plugin, final boolean requirePack) {
        this.plugin = plugin;
        this.requirePack = requirePack;
    }

    @Override
    public void show(final Audience viewer, final RenderedDialog dialog) {
        viewer.showDialog(DialogAdapter.toPaper(dialog));
    }

    @Override
    public void close(final Audience viewer) {
        viewer.closeDialog();
    }

    @Override
    public void schedule(final Audience viewer, final Runnable task) {
        if (viewer instanceof final Player player) {
            // Runs on the player's region thread next tick; silently dropped if the player left.
            player.getScheduler().execute(this.plugin, task, null, 1L);
            return;
        }
        Bukkit.getGlobalRegionScheduler().execute(this.plugin, task);
    }

    @Override
    public boolean supportsGlyphs(final Audience viewer) {
        final UUID id = viewer.get(Identity.UUID).orElse(null);
        if (id == null || this.forcedPlain.contains(id) || BedrockDetector.isBedrock(id)) {
            return false;
        }
        return !this.requirePack || this.packLoaded.contains(id);
    }

    public void packLoaded(final UUID player) {
        this.packLoaded.add(player);
    }

    public void forget(final UUID player) {
        this.packLoaded.remove(player);
        this.forcedPlain.remove(player);
    }

    public void forcePlain(final UUID player, final boolean plain) {
        if (plain) {
            this.forcedPlain.add(player);
            return;
        }
        this.forcedPlain.remove(player);
    }

    public boolean requiresPack() {
        return this.requirePack;
    }
}
