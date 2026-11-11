package me.chubbyduck.canvas.kitchensink.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.CanvasApi;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.elements.Themes;
import me.chubbyduck.canvas.kitchensink.Kitchensink;
import me.chubbyduck.canvas.kitchensink.harness.Harness;
import me.chubbyduck.canvas.paper.CanvasRuntime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/** {@code /ks [page | reopen | theme | plain [on|off] | debug | selftest]}. */
@NullMarked
public final class KitchensinkCommand implements BasicCommand {

    @Override
    public void execute(final CommandSourceStack source, final String[] arguments) {
        final CommandSender sender = source.getSender();
        if (arguments.length > 0 && arguments[0].equals("selftest")) {
            this.selftest(sender);
            return;
        }
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Component.text("Only players can open the kitchensink.", NamedTextColor.RED));
            return;
        }
        if (arguments.length == 0) {
            Kitchensink.index().open(player);
            return;
        }
        switch (arguments[0]) {
            case "reopen" -> {
                final Optional<Canvas> last = Kitchensink.lastClosed(player);
                if (last.isEmpty()) {
                    player.sendMessage(Component.text("Nothing to reopen yet.", NamedTextColor.GRAY));
                    return;
                }
                last.get().open(player);
            }
            case "theme" -> this.theme(player, arguments.length > 1 ? arguments[1] : "");
            case "plain" -> {
                final boolean on = arguments.length < 2 || arguments[1].equalsIgnoreCase("on");
                CanvasRuntime.get().forcePlain(player, on);
                player.sendMessage(Component.text("Plain fallback " + (on ? "forced" : "off"), NamedTextColor.YELLOW));
            }
            case "debug" -> {
                final boolean on = !CanvasRuntime.get().sessions().debug(player.getUniqueId());
                CanvasRuntime.get().debug(player, on);
                player.sendMessage(Component.text("Debug overlay " + (on ? "on" : "off"), NamedTextColor.YELLOW));
            }
            default -> {
                final Optional<Kitchensink.Page> page = Kitchensink.page(arguments[0]);
                if (page.isEmpty()) {
                    player.sendMessage(Component.text("Unknown page " + arguments[0], NamedTextColor.RED));
                    return;
                }
                Kitchensink.canvas(page.get()).open(player);
            }
        }
    }

    @Override
    public Collection<String> suggest(final CommandSourceStack source, final String[] arguments) {
        if (arguments.length <= 1) {
            final List<String> options = new ArrayList<>(List.of("reopen", "theme", "plain", "debug", "selftest"));
            Kitchensink.pages().forEach(page -> options.add(page.id()));
            return options;
        }
        if (arguments[0].equals("theme")) {
            return List.of("ore", "ore_dark");
        }
        if (arguments[0].equals("plain")) {
            return List.of("on", "off");
        }
        return List.of();
    }

    private void theme(final Player player, final String id) {
        final Theme theme = Themes.all().stream().filter(candidate -> candidate.id().equals(id)).findFirst().orElse(null);
        if (theme == null) {
            player.sendMessage(Component.text("Themes: ore, ore_dark", NamedTextColor.RED));
            return;
        }
        final Canvas current = CanvasApi.getCurrentCanvas(player).or(() -> Kitchensink.lastClosed(player)).orElse(Kitchensink.index());
        new Canvas(current.root()).title(current.title()).theme(theme).open(player);
    }

    private void selftest(final CommandSender sender) {
        final Harness harness = new Harness(CanvasRuntime.get().pack());
        int failures = 0;
        for (final Kitchensink.Page page : Kitchensink.pages()) {
            for (final Theme theme : new @org.jspecify.annotations.Nullable Theme[]{Themes.ORE, Themes.ORE_DARK, null}) {
                final long start = System.nanoTime();
                try {
                    final Harness.Result result = harness.render(page.factory().get(), theme, 0);
                    final long micros = (System.nanoTime() - start) / 1000;
                    final int size = result.compiled().dialog().body().stream()
                        .mapToInt(body -> GsonComponentSerializer.gson().serialize(body.text()).length())
                        .sum();
                    if (Themes.ORE.equals(theme)) {
                        sender.sendMessage(Component.text(String.format("%-12s ok  %5d us  %6d chars  %d warnings",
                            page.id(), micros, size, result.warnings().size()), NamedTextColor.GREEN));
                    }
                    for (final String warning : result.warnings()) {
                        sender.sendMessage(Component.text("  " + warning, NamedTextColor.YELLOW));
                    }
                } catch (final RuntimeException exception) {
                    failures++;
                    sender.sendMessage(Component.text(page.id() + " [" + (theme == null ? "plain" : theme.id()) + "] FAILED: "
                        + exception, NamedTextColor.RED));
                }
            }
        }
        sender.sendMessage(Component.text("Selftest done: " + failures + " failures", failures == 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
    }
}
