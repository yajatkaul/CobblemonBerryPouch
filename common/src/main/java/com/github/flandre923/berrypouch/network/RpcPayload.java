package com.github.flandre923.berrypouch.network;

import com.github.flandre923.berrypouch.ModCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RpcPayload(String route, byte[] data) implements CustomPacketPayload {
    public static final Type<RpcPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "rpc"));

    public static final StreamCodec<FriendlyByteBuf, RpcPayload> CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeUtf(packet.route());
                        buf.writeByteArray(packet.data());
                    },
                    buf -> new RpcPayload(buf.readUtf(), buf.readByteArray())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
