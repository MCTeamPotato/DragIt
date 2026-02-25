package me.kall.dragit.network.cache.painting;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.data.painting.SavedPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.painting.PaintingSavePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class PaintingRequestPacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final int hash;

    public PaintingRequestPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, int hash) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.hash = hash;
    }

    public PaintingRequestPacket(@NotNull FriendlyByteBuf buf) {
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

                if (player instanceof ServerPlayer sender) {
                    byte[] bytes = resolveOnServer(sender);
                    if (bytes != null) {
                        DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new PaintingLoadPacket(this.dimension, this.pos, this.textureLocation, bytes));
                    } else {
                        DragIt.LOGGER.warn("[DragIt] Server cannot resolve painting {} (hash={}), client will miss it", this.textureLocation, this.hash);
                    }
                } else {
                    List<byte[]> cached = ImageCache.find(this.hash);
                    if (cached.size() == 1) {
                        DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(this.dimension, this.pos, this.textureLocation, cached.get(0)));
                    } else {
                        DragIt.LOGGER.warn("[DragIt] Client cache has no painting for hash={}, cannot respond to server request", this.hash);
                    }
                }
            } catch (Throwable t) {
                DragIt.LOGGER.error("[DragIt] Error handling PaintingRequestPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }


    private byte @Nullable [] resolveOnServer(@NotNull ServerPlayer sender) {
        List<byte[]> cached = ImageCache.find(this.hash);
        if (cached.size() == 1) return cached.get(0);

        SavedPaintings savedPaintings = SavedPaintings.get(sender.serverLevel());
        var posMap = savedPaintings.paintings.get(this.dimension);
        if (posMap != null) {
            SavedTextureData data = posMap.get(this.pos);
            if (data != null) {
                ImageCache.save(data.textureBytes());
                return data.textureBytes();
            }
        }
        return null;
    }
}