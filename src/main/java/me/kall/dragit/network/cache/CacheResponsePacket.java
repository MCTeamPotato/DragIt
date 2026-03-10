package me.kall.dragit.network.cache;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.cache.PendingRegistrations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CacheResponsePacket {
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public CacheResponsePacket(ResourceLocation textureLocation, byte[] textureBytes) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public CacheResponsePacket(@NotNull FriendlyByteBuf buf) {
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readByteArray();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ImageCache.store(this.textureLocation, this.textureBytes);
                PendingRegistrations.resolve(this.textureLocation, this.textureBytes);
                DragIt.LOGGER.info("CacheResponsePacket: resolved [{}]", this.textureLocation);
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling CacheResponsePacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}