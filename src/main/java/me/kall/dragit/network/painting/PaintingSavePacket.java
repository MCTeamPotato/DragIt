package me.kall.dragit.network.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.kall.dragit.data.painting.SavedPaintings;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.PaintingPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PaintingSavePacket extends PaintingPacket {
    public PaintingSavePacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] textureBytes) {
        super(dimension, pos, textureLocation, textureBytes);
    }

    public PaintingSavePacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SavedPaintings savedPaintings = SavedPaintings.get(player.serverLevel());
            savedPaintings.setDirty();
            savedPaintings.paintings.computeIfAbsent(this.dimension, key -> new Long2ObjectOpenHashMap<>()).put(this.pos, new SavedTextureData(this.textureLocation, this.textureBytes));

            List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
            if (syncTargets.size() == 1) return;
            UUID uuid = player.getUUID();
            for (ServerPlayer syncTarget : syncTargets) {
                if (syncTarget.getUUID().equals(uuid)) continue;
                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new PaintingLoadPacket(this.dimension, this.pos, this.textureLocation, this.textureBytes));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
