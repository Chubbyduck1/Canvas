package me.chubbyduck.canvas.session;

import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface CloseHandler {

    void onClose(Audience viewer, CloseReason reason);

}
