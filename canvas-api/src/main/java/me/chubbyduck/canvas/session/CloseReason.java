package me.chubbyduck.canvas.session;

import org.jspecify.annotations.NullMarked;

/** Why a session closed. */
@NullMarked
public enum CloseReason {

    /** The player pressed escape (or the dialog's exit button). */
    ESCAPE,
    /** {@link Session#close()} was called. */
    CLOSED_BY_CODE,
    /** Another canvas was opened for the player. */
    REPLACED,
    /** Another plugin's dialog or an inventory opened over it. */
    EXTERNAL,
    /** The player left the server. */
    QUIT

}
