package com.github.flandre923.berrypouch.network;

import com.github.flandre923.berrypouch.ModCommon;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class PacketInvoker {

    private PacketInvoker() {
    }

    public static void sendToServer(CustomPacketPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            ModCommon.LOG.warn("Attempted to send {} when not in a valid client context!", payload.type().id());
            return;
        }
        NetworkManager.sendToServer(payload);
    }

    public static void sendOpenPouch() {
        RpcRouter.sendToServer(RpcRoutes.OPEN_POUCH);
    }

    public static void sendToggleMarkSlot(int slotIndex) {
        RpcRouter.sendToServer(RpcRoutes.TOGGLE_MARK_SLOT, buf -> buf.writeInt(slotIndex));
    }

    public static void sendCycleBait(boolean isMainHand, boolean isLeftCycle) {
        RpcRouter.sendToServer(RpcRoutes.CYCLE_BAIT, buf -> {
            buf.writeBoolean(isMainHand);
            buf.writeBoolean(isLeftCycle);
        });
    }

    public static void sendToggleAutoBerry() {
        RpcRouter.sendToServer(RpcRoutes.TOGGLE_AUTO_BERRY);
    }
}

