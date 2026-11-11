package me.chubbyduck.canvas.core.dialog;

import java.util.ArrayList;
import java.util.List;
import me.chubbyduck.canvas.core.render.ResolvedNode;
import me.chubbyduck.canvas.node.NodeInternals;
import me.chubbyduck.canvas.paint.DialogAction;
import me.chubbyduck.canvas.paint.DialogInput;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Compiles a resolved tree into a vanilla-only dialog for players without the resource pack (or on BedrockDetector):
 * text lines in the body, buttons in the footer, native inputs unchanged.
 */
@NullMarked
public class PlainCompiler {

    private static final int BODY_WIDTH = 300;
    private static final int BUTTON_WIDTH = 150;

    public CompiledDialog compile(
        final ResolvedNode root,
        final Component title,
        final boolean closeOnEscape,
        final String session,
        final int nonce
    ) {
        final NativeDialogParts parts = new NativeDialogParts(session, nonce);
        final List<RenderedDialog.Body> body = new ArrayList<>();
        final List<DialogAction> footers = new ArrayList<>();
        this.visit(root, parts, body, footers);
        for (final DialogAction footer : footers) {
            parts.footer(footer);
        }
        if (body.isEmpty()) {
            body.add(new RenderedDialog.Body(Component.empty(), BODY_WIDTH));
        }
        return parts.finish(title, body, 2, closeOnEscape);
    }

    private void visit(
        final ResolvedNode node,
        final NativeDialogParts parts,
        final List<RenderedDialog.Body> body,
        final List<DialogAction> footers
    ) {
        NodeInternals.plain(node.node(), new PlainContext() {
            @Override
            public void renderChildren() {
                for (final ResolvedNode child : node.children()) {
                    PlainCompiler.this.visit(child, parts, body, footers);
                }
            }

            @Override
            public void text(final Component text) {
                body.add(new RenderedDialog.Body(text, BODY_WIDTH));
            }

            @Override
            public void button(final Component label, final @Nullable Component tooltip, final Runnable onClick) {
                parts.button(label, tooltip, BUTTON_WIDTH, onClick);
            }

            @Override
            public void input(final DialogInput input) {
                parts.input(input);
            }

            @Override
            public void footer(final DialogAction action) {
                footers.add(action);
            }
        });
    }
}
