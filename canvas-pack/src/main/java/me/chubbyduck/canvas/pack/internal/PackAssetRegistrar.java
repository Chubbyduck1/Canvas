package me.chubbyduck.canvas.pack.internal;

import java.util.Map;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.NineSlicePainter;
import me.chubbyduck.canvas.asset.Painter;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PackAssetRegistrar implements AssetRegistrar {

    private final Class<? extends Node> owner;
    private final Map<Key, Painter> sprites;
    private final Map<Key, NineSlicePainter> skins;
    private final Map<Key, Class<?>> owners;

    public PackAssetRegistrar(
        final Class<? extends Node> owner,
        final Map<Key, Painter> sprites,
        final Map<Key, NineSlicePainter> skins,
        final Map<Key, Class<?>> owners
    ) {
        this.owner = owner;
        this.sprites = sprites;
        this.skins = skins;
        this.owners = owners;
    }

    @Override
    public Sprite registerSprite(final Key key, final Painter painter) {
        this.claim(key);
        this.sprites.put(key, painter);
        return new Sprite(key);
    }

    @Override
    public NineSlice registerNineSlice(final Key key, final NineSlicePainter painter) {
        this.claim(key);
        this.skins.put(key, painter);
        return new NineSlice(key);
    }

    private void claim(final Key key) {
        final Class<?> previous = this.owners.putIfAbsent(key, this.owner);
        if (previous == null) {
            return;
        }
        throw new IllegalStateException(
            "Asset " + key.asString() + " registered twice (by " + previous.getName() + " and " + this.owner.getName() + ")");
    }

}
