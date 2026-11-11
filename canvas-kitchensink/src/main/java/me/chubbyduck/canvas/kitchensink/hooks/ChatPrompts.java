package me.chubbyduck.canvas.kitchensink.hooks;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

/** Paper implementation of the kitchensink hooks: chat prompts and opening an inventory. */
@NullMarked
public final class ChatPrompts implements KitchensinkHooks, Listener {

    private final Plugin plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public ChatPrompts(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void askChat(final Audience viewer, final String prompt, final Consumer<String> answer) {
        if (!(viewer instanceof final Player player)) {
            return;
        }
        this.pending.put(player.getUniqueId(), answer);
        player.closeDialog();
        player.sendMessage(Component.text(prompt + " (your next chat message is captured)", NamedTextColor.YELLOW));
    }

    @Override
    public void openInventory(final Audience viewer) {
        if (viewer instanceof final Player player) {
            player.openInventory(Bukkit.createInventory(null, 27, Component.text("Close me, then /ks reopen")));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final Consumer<String> answer = this.pending.remove(player.getUniqueId());
        if (answer == null) {
            return;
        }
        event.setCancelled(true);
        final String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        player.getScheduler().execute(this.plugin, () -> answer.accept(text), null, 1L);
    }
}
