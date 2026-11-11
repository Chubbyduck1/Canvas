package me.chubbyduck.canvas.paper;

import me.chubbyduck.canvas.elements.Nodes;
import me.chubbyduck.canvas.elements.Themes;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

/**
 * The standalone Canvas plugin; dependents register nodes with {@code CanvasRuntime.get().register(...)}.
 */
@NullMarked
public class CanvasPlugin extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 35261;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        CanvasRuntime.bootstrap(this)
            .registerTheme(Themes.ORE, Themes.ORE_DARK)
            .register(Nodes.defaults())
            .requirePack(this.getConfig().getBoolean("require-pack", false))
            .start();
        if (!this.getConfig().getBoolean("metrics", true)) {
            return;
        }
        new Metrics(this, BSTATS_PLUGIN_ID);
    }

}
