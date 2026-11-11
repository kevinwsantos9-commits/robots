package com.cyberparts;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class Net {
    /** Servidor -> cliente: abre o menu da bancada. */
    public record Open() implements CustomPayload {
        public static final CustomPayload.Id<Open> ID = new CustomPayload.Id<>(Identifier.of(CyberParts.MOD, "open"));
        public static final PacketCodec<ByteBuf, Open> CODEC = PacketCodec.unit(new Open());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Servidor -> cliente: máscara de peças instaladas. */
    public record Sync(int mask) implements CustomPayload {
        public static final CustomPayload.Id<Sync> ID = new CustomPayload.Id<>(Identifier.of(CyberParts.MOD, "sync"));
        public static final PacketCodec<ByteBuf, Sync> CODEC = PacketCodecs.VAR_INT.xmap(Sync::new, Sync::mask);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Cliente -> servidor: instalar/remover uma peça num nível (1..3). */
    public record Toggle(int part, int tier) implements CustomPayload {
        public static final CustomPayload.Id<Toggle> ID = new CustomPayload.Id<>(Identifier.of(CyberParts.MOD, "toggle"));
        public static final PacketCodec<ByteBuf, Toggle> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Toggle::part, PacketCodecs.VAR_INT, Toggle::tier, Toggle::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
}
