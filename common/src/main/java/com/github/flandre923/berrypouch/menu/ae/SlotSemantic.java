package com.github.flandre923.berrypouch.menu.ae;

/**
 * Used to group slots in a menu into semantics, which are then positioned by a {@code ScreenStyle}.
 *
 * @param id semantic id
 * @param playerSide whether this semantic belongs to player-carried inventory
 * @param quickMovePriority higher priority is preferred as quick-move destination
 */
public record SlotSemantic(String id, boolean playerSide, int quickMovePriority) {
}
