package com.github.flandre923.berrypouch.menu.container;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.menu.layout.PokeBallGunLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunHelper;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PokeBallGunContainer extends AbstractContainerMenu {
    private final ItemStack gunStack;
    private final PokeBallGunInventory  gunInventory;

    // 用于同步 selectedIndex 的 DataSlot
    private int selectedIndex;

    private static final int GUN_SLOTS = 9;

    public PokeBallGunContainer(int containerId, Inventory playerInv, ItemStack gunStack) {
        super(ModRegistries.ModMenuTypes.POKEBALL_GUN_MENU.get(), containerId);
        this.gunStack = gunStack;
        this.gunInventory = new PokeBallGunInventory(gunStack,GUN_SLOTS);


        // 添加 DataSlot 用于同步
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PokeBallGunHelper.getSelectedIndex(gunStack);
            }

            @Override
            public void set(int value) {
                selectedIndex = value;
                PokeBallGunHelper.updateSelectedItemId(gunStack);
            }
        });

        // 添加发射器槽位 (1行8列)
        addGunInventory();
        // 添加玩家背包槽位
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);


    }

    public static PokeBallGunContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        return new PokeBallGunContainer(windowId,inv,ItemStack.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < GUN_SLOTS) {
                // 从发射器移到背包
                if (!this.moveItemStackTo(stackInSlot, GUN_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从背包移到发射器（只移动捕捉球）
                if (PokeBallSlot.isPokeBall(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, GUN_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            PokeBallGunHelper.updateSelectedItemId(gunStack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return !gunStack.isEmpty();
    }


    private void addGunInventory(){
        for (DeclarativeStorageLayout.SlotSpec slot : PokeBallGunLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.GUN_AMMO)) {
            addSlot(new PokeBallSlot(gunInventory, slot.index(), slot.x(), slot.y()));
        }
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (DeclarativeStorageLayout.SlotSpec slot : PokeBallGunLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.PLAYER_INVENTORY)) {
            addSlot(new Slot(playerInv, slot.index(), slot.x(), slot.y()));
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (DeclarativeStorageLayout.SlotSpec slot : PokeBallGunLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.PLAYER_HOTBAR)) {
            addSlot(new Slot(playerInv, slot.index(), slot.x(), slot.y()));
        }
    }


    public class PokeBallSlot extends Slot {

        public PokeBallSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isPokeBall(stack);
        }

        public static boolean isPokeBall(ItemStack stack) {
            if (stack.isEmpty()) return false;

            // 方法1: 检查物品是否属于 Cobblemon 的 PokeBall 类
            if (stack.getItem() instanceof PokeBallItem) return true;

            // 方法2: 使用 Tag 检查（推荐，更灵活）
//            return stack.is(ModTags.Items.POKEBALLS);

            // 方法3: 检查物品 ID 前缀
            // ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            // return id.getNamespace().equals("cobblemon") && id.getPath().contains("poke_ball");
            return false;
        }

    }
    public SimpleContainer getGunInventory() {
        return this.gunInventory;
    }

    public ItemStack getGunStack() {
        return this.gunStack;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }
}
