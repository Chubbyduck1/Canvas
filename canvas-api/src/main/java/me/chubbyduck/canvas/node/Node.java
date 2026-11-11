package me.chubbyduck.canvas.node;

import java.util.List;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import me.chubbyduck.canvas.state.Scope;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base class of every built-in primitive: drawing widgets and containers.
 */
@NullMarked
public abstract class Node implements NodeLike {

    private @Nullable String key;
    private Length width = Length.HUG;
    private Length height = Length.HUG;
    private float grow;
    private float shrink = 1;
    private int basis = -1;
    private int minWidth;
    private int minHeight;
    private @Nullable Align alignSelf;
    private int offsetX;
    private int offsetY;
    private int z;

    @Override
    public Node getNode(final Scope scope) {
        return this;
    }

    @Override
    public @Nullable String key() {
        return this.key;
    }

    /**
     * Child elements of this node, in paint order.
     */
    public List<? extends NodeLike> children() {
        return List.of();
    }

    /**
     * Declares this node class's textures.
     */
    protected void register(final AssetRegistrar assets) {
    }

    /**
     * Intrinsic ("hug") size of this node.
     */
    protected Size measure(final LayoutContext context) {
        return Size.ZERO;
    }

    /**
     * Places children inside the given rectangle.
     */
    protected void arrange(final Rect rect, final LayoutContext context) {
    }

    /**
     * Emits draw operations, hit regions, native inputs and footer buttons for this node.
     */
    protected void paint(final PaintContext context) {
    }

    /**
     * Renders this node for players without the resource pack.
     */
    protected void plain(final PlainContext context) {
        context.renderChildren();
    }

    public Node key(final @Nullable String key) {
        this.key = key;
        return this;
    }

    public Node width(final int pixels) {
        return this.width(Length.px(pixels));
    }

    public Node width(final Length width) {
        this.width = width;
        return this;
    }

    public Node height(final int pixels) {
        return this.height(Length.px(pixels));
    }

    public Node height(final Length height) {
        this.height = height;
        return this;
    }

    public Node size(final int width, final int height) {
        return this.width(width).height(height);
    }

    /**
     * The narrowest this node may be.
     */
    public Node minWidth(final int pixels) {
        this.minWidth = Math.max(0, pixels);
        return this;
    }

    /**
     * The shortest this node may be.
     */
    public Node minHeight(final int pixels) {
        this.minHeight = Math.max(0, pixels);
        return this;
    }

    public int minWidth() {
        return this.minWidth;
    }

    public int minHeight() {
        return this.minHeight;
    }

    /**
     * Fills the available space on both axes.
     */
    public Node fill() {
        return this.width(Length.FILL).height(Length.FILL);
    }

    public Node grow(final float grow) {
        this.grow = grow;
        return this;
    }

    public Node shrink(final float shrink) {
        this.shrink = shrink;
        return this;
    }

    /**
     * The main-axis size a flex container starts from before growing or shrinking this node, like CSS {@code
     * flex-basis}.
     */
    public Node basis(final int basis) {
        this.basis = basis;
        return this;
    }

    public int basis() {
        return this.basis;
    }

    public Node alignSelf(final @Nullable Align align) {
        this.alignSelf = align;
        return this;
    }

    /**
     * Shifts this node visually without affecting its siblings' layout.
     */
    public Node offset(final int offsetX, final int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    /**
     * Paint order relative to siblings; higher draws on top.
     */
    public Node z(final int z) {
        this.z = z;
        return this;
    }

    public Length width() {
        return this.width;
    }

    public Length height() {
        return this.height;
    }

    public float grow() {
        return this.grow;
    }

    public float shrink() {
        return this.shrink;
    }

    public @Nullable Align alignSelf() {
        return this.alignSelf;
    }

    public int offsetX() {
        return this.offsetX;
    }

    public int offsetY() {
        return this.offsetY;
    }

    public int z() {
        return this.z;
    }

}
