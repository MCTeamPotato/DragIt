package me.kall.dragit.network.base;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class Handler {
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> this.handle(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    public abstract void handle(@Nullable ServerPlayer player);
}
