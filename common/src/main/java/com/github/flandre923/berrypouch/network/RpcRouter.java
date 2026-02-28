package com.github.flandre923.berrypouch.network;

import com.github.flandre923.berrypouch.ModCommon;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class RpcRouter {
    @FunctionalInterface
    public interface ServerHandler {
        void handle(ServerPlayer player, FriendlyByteBuf data);
    }

    private static final Map<String, ServerHandler> SERVER_HANDLERS = new ConcurrentHashMap<>();

    private RpcRouter() {
    }

    public static void registerServer(String route, ServerHandler handler) {
        ServerHandler old = SERVER_HANDLERS.putIfAbsent(route, handler);
        if (old != null) {
            throw new IllegalStateException("Duplicate RPC route registration: " + route);
        }
    }

    public static void handle(RpcPayload payload, dev.architectury.networking.NetworkManager.PacketContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        context.queue(() -> {
            ServerHandler handler = SERVER_HANDLERS.get(payload.route());
            if (handler == null) {
                ModCommon.LOG.warn("Unknown RPC route from {}: {}", player.getName().getString(), payload.route());
                return;
            }
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
            handler.handle(player, data);
        });
    }

    public static void sendToServer(String route) {
        sendToServer(route, null);
    }

    public static void sendToServer(String route, @Nullable Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        if (writer != null) {
            writer.accept(data);
        }
        byte[] bytes = new byte[data.readableBytes()];
        data.getBytes(0, bytes);
        PacketInvoker.sendToServer(new RpcPayload(route, bytes));
    }
}
