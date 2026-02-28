package com.github.flandre923.berrypouch.menu.container;

import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.helper.PouchItemHelper;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.layout.LargeBerryPouchLayout;
import com.github.flandre923.berrypouch.menu.slot.BerryPouchSlot;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LargeBerryPouchContainer extends  AbstractBerryPouchContainer {
    private final ItemStack pouchStack;

    public LargeBerryPouchContainer(int windowId, Inventory playerInv, ItemStack pouchStack ,int openFlag) {
        super(ModRegistries.ModMenuTypes.BERRY_POUCH_CONTAINER_69.get(),
                windowId, playerInv, pouchStack, BerryPouchType.LARGE,openFlag);
        this.pouchStack = pouchStack;
    }
        
    // 辅助方法：判断物品是否是树果
    public static boolean isBerry(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // 使用cobblemon的berries标签来判断是否是树果
        return stack.is(net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cobblemon", "berries")
        ));
    }
    
    // 辅助方法：判断物品是否是other_baits
    public static boolean isOtherBaits(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("berrypouch", "other_baits")
        ));
    }
    public static LargeBerryPouchContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
//        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int isHand = buf.readInt();
        ItemStack item;
        if(isHand  == 0){
            item = inv.player.getItemInHand(InteractionHand.MAIN_HAND);
        }else if(isHand == 1){
            item = inv.player.getItemInHand(InteractionHand.OFF_HAND);
        }else{
            item = PouchItemHelper.findBerryPouch(inv.player);
        }
        return new LargeBerryPouchContainer(windowId, inv,item,isHand);
    }

    @Override
    protected void addPouchSlots() {
        for (DeclarativeStorageLayout.SlotSpec slot : LargeBerryPouchLayout.STORAGE.slots()) {
            if (slot.role() == DeclarativeStorageLayout.SlotRole.POUCH_BERRY) {
                addBerrySlot(slot.index(), slot.x(), slot.y());
            } else if (slot.role() == DeclarativeStorageLayout.SlotRole.POUCH_OTHER_BAIT) {
                addOtherBaitsSlot(slot.index(), slot.x(), slot.y());
            }
        }
    }

    private void addBerrySlot(int slotIndex, int x, int y) {
        addSlot(new BerryPouchSlot(pouchInventory, slotIndex, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 使用辅助方法判断是否是树果，并且检查是否匹配特定槽位
                return isBerry(stack) && BerryPouchType.LARGE.getStorageSlot()
                        .matchesSlotItem(slotIndex + 1, stack.getItem());
            }
        });
    }
    
    private void addOtherBaitsSlot(int slotIndex, int x, int y) {
        addSlot(new BerryPouchSlot(pouchInventory, slotIndex, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 使用辅助方法判断是否是other_baits
                return isOtherBaits(stack);
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack originalStack = clickedSlot.getItem();
        ItemStack returnStack = originalStack.copy();

        if (slotIndex < BerryPouchType.LARGE.getSize()) {
            int maxMove = Math.min(64, originalStack.getItem().getDefaultMaxStackSize());
            int currentCount = originalStack.getCount();
            int toMove = Math.min(currentCount, maxMove);

            ItemStack toTransfer = originalStack.copyWithCount(toMove);
            boolean moved = this.moveItemStackTo(toTransfer, BerryPouchType.LARGE.getSize(), this.slots.size(), true);

            if (moved) {                // 从树果袋移除实际移动的数量
                int actuallyMoved = toMove - toTransfer.getCount();
                // 如果背包已满，实际移除的可能比预期的少
                if (actuallyMoved > 0) {
                    pouchInventory.removeFromSlot(slotIndex, actuallyMoved);
                    // 返回实际移动后的副本，用于同步客户端
                    ItemStack finalReturn = returnStack.copy();
                    finalReturn.setCount(actuallyMoved);
                    return ItemStack.EMPTY;
                }
            }
            return ItemStack.EMPTY; // 如果完全没法移动（比如背包满），返回空
        } else {
            // 从背包移到袋子
            boolean moved = false;

            if (BerryPouchType.LARGE.getStorageSlot().has(originalStack.getItem())) {
                moved = this.moveItemStackToPouch(originalStack, 0, 70);
            }

            if (!moved && originalStack.is(net.minecraft.tags.TagKey.create(
                    net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("berrypouch", "other_baits")
            ))) {
                moved = this.moveItemStackToPouch(originalStack, 70, 86);
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }

            // 更新源槽位
            clickedSlot.set(originalStack);
        }

        return returnStack;
    }


    // 自定义方法：移动物品到袋子（支持超堆叠）
    private boolean moveItemStackToPouch(ItemStack stack, int startSlot, int endSlot) {

        boolean moved = false;

        // 先堆叠到同类物品
        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getItem();

            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int toAdd = stack.getCount();
                this.pouchInventory.addToSlot(i, toAdd);
                stack.setCount(0);
                moved = true;
            }
        }

        // 再放入空槽位
        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                this.pouchInventory.setItem(i, stack.copy());
                stack.setCount(0);
                moved = true;
            }
        }

        return moved;
    }
}
