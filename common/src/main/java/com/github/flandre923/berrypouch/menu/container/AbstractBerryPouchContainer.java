package com.github.flandre923.berrypouch.menu.container;

import com.github.flandre923.berrypouch.helper.PouchItemHelper;
import com.github.flandre923.berrypouch.item.BerryPouch;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchInventory;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchManager;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.ae.AEBaseMenu;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import com.github.flandre923.berrypouch.menu.slot.SlotLocked;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractBerryPouchContainer extends AEBaseMenu {
    protected final ItemStack pouchStack;
    protected final BerryPouchInventory pouchInventory;
    protected final BerryPouchType pouchType;
    protected final int openFlag;

    public AbstractBerryPouchContainer(
            MenuType<?> type, int windowId, Inventory playerInv,
            ItemStack pouchStack, BerryPouchType pouchType, int openFlag
    ) {
        super(type, windowId, playerInv);
        this.pouchStack = pouchStack;
        this.pouchType = pouchType;
        this.pouchInventory = BerryPouchManager.getInventory(pouchStack, playerInv.player.level());
        this.openFlag = openFlag;

        addPouchSlots();
        addPlayerSlots(playerInv, pouchStack);
    }

    protected abstract void addPouchSlots();

    protected void addPlayerSlots(Inventory playerInv, ItemStack bag) {
        addInventorySlots(playerInv);
        addHotbarSlots(playerInv, bag);
    }

    @Override
    public boolean stillValid(Player player) {
        return BerryPouchManager.isHoldingPouch(player, pouchStack) || !PouchItemHelper.findBerryPouch(player).isEmpty();
    }

    private void addInventorySlots(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 0, 0), SlotSemantics.PLAYER_INVENTORY);
            }
        }
    }

    private void addHotbarSlots(Inventory playerInv, ItemStack bag) {
        for (int i = 0; i < 9; ++i) {
            if (bag.getItem() instanceof BerryPouch && playerInv.getItem(i) == bag) {
                addSlot(new SlotLocked(playerInv, i, 0, 0), SlotSemantics.PLAYER_HOTBAR);
            } else {
                addSlot(new Slot(playerInv, i, 0, 0), SlotSemantics.PLAYER_HOTBAR);
            }
        }
    }

    public Container getPouchInventory() {
        return this.pouchInventory;
    }

    public ItemStack getPouchStack() {
        return this.pouchStack;
    }

    public BerryPouchType getPouchType() {
        return this.pouchType;
    }

    public int getOpenFlag() {
        return openFlag;
    }
}