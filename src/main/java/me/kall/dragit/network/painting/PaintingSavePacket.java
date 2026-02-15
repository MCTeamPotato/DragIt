package me.kall.dragit.network.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.kall.dragit.data.painting.SavedPaintings;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class PaintingSavePacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public PaintingSavePacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] textureBytes) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public PaintingSavePacket(@NotNull FriendlyByteBuf buf) {
        this.dimension = buf.readResourceLocation();
        this.pos = buf.readLong();
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readByteArray();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.dimension);
        buf.writeLong(this.pos);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SavedPaintings savedPaintings = SavedPaintings.get(player.serverLevel());
            savedPaintings.setDirty();
            savedPaintings.paintings.computeIfAbsent(this.dimension, key -> new Long2ObjectOpenHashMap<>()).put(this.pos, new SavedPaintings.Painting(this.textureLocation, this.textureBytes));
            List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
            if (syncTargets.size() == 1) return;
            for (ServerPlayer syncTarget : syncTargets) {
                if (syncTarget.getUUID().equals(player.getUUID())) continue;
                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new PaintingLoadPacket(this.dimension, this.pos, this.textureLocation, this.textureBytes));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
