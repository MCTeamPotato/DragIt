package me.kall.dragit.network.base;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Handler implements CustomPacketPayload {
    public void handle(@NotNull IPayloadContext ctx) {
        ctx.enqueueWork(() -> this.handle(ctx.player() instanceof ServerPlayer sp ? sp : null));
    }

    public abstract void handle(@Nullable ServerPlayer player);
}