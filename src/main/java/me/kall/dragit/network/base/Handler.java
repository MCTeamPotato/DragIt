package me.kall.dragit.network.base;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public abstract class Handler implements CustomPacketPayload {
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> this.handle(context.player() instanceof ServerPlayer player ? player : null));
    }

    public abstract void handle(@Nullable ServerPlayer player);
}
