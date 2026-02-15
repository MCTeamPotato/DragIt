package me.kall.dragit.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.kall.dragit.data.SavedImages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ImageSavePacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final byte[] dynamicTexture;

    public ImageSavePacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] dynamicTexture) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.dynamicTexture = dynamicTexture;
    }

    public ImageSavePacket(@NotNull FriendlyByteBuf buf) {
        this.dimension = buf.readResourceLocation();
        this.pos = buf.readLong();
        this.textureLocation = buf.readResourceLocation();
        this.dynamicTexture = buf.readByteArray();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.dimension);
        buf.writeLong(this.pos);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.dynamicTexture);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SavedImages savedImages = SavedImages.get(player.serverLevel());
            savedImages.setDirty();
            savedImages.images.computeIfAbsent(this.dimension, key -> new Long2ObjectOpenHashMap<>()).put(this.pos, new SavedImages.Image(this.textureLocation, this.dynamicTexture));
        });
        ctx.get().setPacketHandled(true);
    }
}
