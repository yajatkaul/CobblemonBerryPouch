package com.github.flandre923.berrypouch.network;

import com.github.flandre923.berrypouch.network.handler.CycleBaitRpcHandler;
import com.github.flandre923.berrypouch.network.handler.OpenPouchRpcHandler;
import com.github.flandre923.berrypouch.network.handler.ToggleAutoBerryRpcHandler;
import com.github.flandre923.berrypouch.network.handler.ToggleMarkSlotRpcHandler;
import dev.architectury.networking.NetworkManager;

public class ModNetworking {

    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                RpcPayload.TYPE,
                RpcPayload.CODEC,
                RpcRouter::handle
        );

        RpcRouter.registerServer(RpcRoutes.OPEN_POUCH, (player, data) -> OpenPouchRpcHandler.handle(player));
        RpcRouter.registerServer(RpcRoutes.CYCLE_BAIT, (player, data) -> CycleBaitRpcHandler.handle(player, data.readBoolean(), data.readBoolean()));
        RpcRouter.registerServer(RpcRoutes.TOGGLE_MARK_SLOT, (player, data) -> ToggleMarkSlotRpcHandler.handle(player, data.readInt()));
        RpcRouter.registerServer(RpcRoutes.TOGGLE_AUTO_BERRY, (player, data) -> ToggleAutoBerryRpcHandler.handle(player));
    }
}
