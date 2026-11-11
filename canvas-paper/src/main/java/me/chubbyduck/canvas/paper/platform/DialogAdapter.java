package me.chubbyduck.canvas.paper.platform;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.List;
import me.chubbyduck.canvas.core.dialog.RenderedDialog;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.jspecify.annotations.NullMarked;

/** Converts the platform-neutral {@link RenderedDialog} into a Paper {@link Dialog}. */
@NullMarked
public final class DialogAdapter {

    private DialogAdapter() {
    }

    static Dialog toPaper(final RenderedDialog dialog) {
        final List<DialogBody> body = dialog.body().stream()
            .map(element -> (DialogBody) DialogBody.plainMessage(element.text(), element.width()))
            .toList();
        final List<DialogInput> inputs = dialog.inputs().stream().map(DialogAdapter::input).toList();
        final DialogBase base = DialogBase.builder(dialog.title())
            .canCloseWithEscape(dialog.closeOnEscape())
            .pause(false)
            .afterAction(DialogBase.DialogAfterAction.NONE)
            .body(body)
            .inputs(inputs)
            .build();
        final ActionButton exit = button(dialog.exit());
        final DialogType type = dialog.buttons().isEmpty()
            ? DialogType.notice(exit)
            : DialogType.multiAction(dialog.buttons().stream().map(DialogAdapter::button).toList())
            .columns(dialog.columns())
            .exitAction(exit)
            .build();
        return Dialog.create(builder -> builder.empty().base(base).type(type));
    }

    private static ActionButton button(final RenderedDialog.Button data) {
        return ActionButton.builder(data.label())
            .tooltip(data.tooltip())
            .width(data.width())
            // A "dynamic" custom action: the client adds every input's value to the payload.
            .action(DialogAction.customClick(RenderedDialog.ACTION, BinaryTagHolder.binaryTagHolder(data.payload())))
            .build();
    }

    private static DialogInput input(final RenderedDialog.Input rendered) {
        final String key = rendered.key();
        return switch (rendered.input()) {
            case final me.chubbyduck.canvas.paint.DialogInput.Text text -> DialogInput.text(key, text.label())
                .labelVisible(text.labelVisible())
                .initial(text.initial())
                .maxLength(text.maxLength())
                .width(text.width())
                .build();
            case final me.chubbyduck.canvas.paint.DialogInput.Bool bool -> DialogInput.bool(key, bool.label())
                .initial(bool.initial())
                .build();
            case final me.chubbyduck.canvas.paint.DialogInput.Range range ->
                DialogInput.numberRange(key, range.label(), range.start(), range.end())
                    .labelFormat(range.labelFormat())
                    .step(range.step())
                    .initial(range.initial())
                    .width(range.width())
                    .build();
            case final me.chubbyduck.canvas.paint.DialogInput.Options options -> DialogInput.singleOption(
                key,
                options.label(),
                options.options().stream()
                    .map(option -> SingleOptionDialogInput.OptionEntry.create(
                        option.id(), option.display(), option.id().equals(options.initialId())))
                    .toList()
            )
                .labelVisible(options.labelVisible())
                .width(options.width())
                .build();
        };
    }
}
