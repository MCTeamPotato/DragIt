package me.kall.dragit.network.cache;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.Handler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class CacheRequestPacket extends Handler {
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

    public void handle(ServerPlayer player) {
        try {
            if (player == null) return;
            byte[] bytes = ImageCache.load(this.textureLocation);
            if (bytes == null) {
                DragIt.LOGGER.warn("CacheRequestPacket: no cache entry for [{}], cannot respond.", this.textureLocation);
                return;
            }
            DragNetworker.send(player, new CacheResponsePacket(this.textureLocation, bytes));
            DragIt.LOGGER.info("CacheRequestPacket: sent {} bytes for [{}] to {}", bytes.length, this.textureLocation, player.getName().getString());
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling CacheRequestPacket", t);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return DragNetworker.CACHE_REQUEST_TYPE;
    }
}