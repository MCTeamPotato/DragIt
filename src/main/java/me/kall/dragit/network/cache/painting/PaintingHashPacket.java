package me.kall.dragit.network.cache.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.data.painting.SavedPaintings;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PaintingHashPacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final int hash;

    public PaintingHashPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, int hash) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.hash = hash;
    }

    public PaintingHashPacket(@NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readLong(), buf.readResourceLocation(), buf.readInt());
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.dimension);
        buf.writeLong(this.pos);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeInt(this.hash);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                Player player = ctx.get().getSender();
                List<byte[]> cached = ImageCache.find(this.hash);

                if (player instanceof ServerPlayer sender) {
                    if (cached.size() == 1) {
                        byte[] bytes = cached.get(0);
                        SavedPaintings savedPaintings = SavedPaintings.get(sender.serverLevel());
                        savedPaintings.setDirty();
                        savedPaintings.paintings.computeIfAbsent(this.dimension, k -> new Long2ObjectOpenHashMap<>()).put(this.pos, new SavedTextureData(this.textureLocation, bytes));
                        UUID uuid = sender.getUUID();
                        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
                            if (target.getUUID().equals(uuid)) continue;
                            DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new PaintingHashPacket(this.dimension, this.pos, this.textureLocation, this.hash));
                        }
                        DragIt.LOGGER.info("[DragIt] Server cache hit, painting processed: {}", this.textureLocation);
                    } else {
                        DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new PaintingRequestPacket(this.dimension, this.pos, this.textureLocation, this.hash));
                    }
                } else {
                    if (cached.size() == 1) {
                        ClientPaintings.registerPainting(this.dimension, this.pos, cached.get(0), this.textureLocation);
                        DragIt.LOGGER.info("[DragIt] Client cache hit, painting registered: {}", this.textureLocation);
                    } else {
                        DragNetworker.INSTANCE.sendToServer(new PaintingRequestPacket(this.dimension, this.pos, this.textureLocation, this.hash));
                    }
                }
            } catch (Throwable t) {
                DragIt.LOGGER.error("[DragIt] Error handling PaintingHashPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}