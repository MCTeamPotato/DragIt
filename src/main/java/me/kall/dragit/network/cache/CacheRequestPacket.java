package me.kall.dragit.network.cache;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CacheRequestPacket {
    public final ResourceLocation textureLocation;

    public CacheRequestPacket(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }

    public CacheRequestPacket(@NotNull FriendlyByteBuf buf) {
        this.textureLocation = buf.readResourceLocation();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.textureLocation);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                byte[] bytes = ImageCache.load(this.textureLocation);
                if (bytes == null) {
                    DragIt.LOGGER.warn("CacheRequestPacket: no cache entry for [{}], cannot respond.", this.textureLocation);
                    return;
                }
                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CacheResponsePacket(this.textureLocation, bytes));
                DragIt.LOGGER.info("CacheRequestPacket: sent {} bytes for [{}] to {}", bytes.length, this.textureLocation, player.getName().getString());
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling CacheRequestPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}