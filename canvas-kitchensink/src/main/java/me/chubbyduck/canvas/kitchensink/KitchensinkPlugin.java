package me.chubbyduck.canvas.kitchensink;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.chubbyduck.canvas.kitchensink.command.KitchensinkCommand;
import me.chubbyduck.canvas.kitchensink.hooks.ChatPrompts;
import me.chubbyduck.canvas.kitchensink.node.Flags;
import me.chubbyduck.canvas.kitchensink.node.Gauge;
import me.chubbyduck.canvas.paper.CanvasRuntime;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

/** The Canvas test plugin: every element and feature on its own page. */
@NullMarked
public class KitchensinkPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        CanvasRuntime.get().register(Gauge.class, Flags.class);

        final ChatPrompts prompts = new ChatPrompts(this);
        Kitchensink.hooks(prompts);
        Bukkit.getPluginManager().registerEvents(prompts, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register("ks", "Open the Canvas kitchensink", new KitchensinkCommand()));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        Kitchensink.forget(event.getPlayer().getUniqueId());
    }
}
