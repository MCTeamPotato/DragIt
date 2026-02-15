package me.kall.dragit.network;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.ClientImages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Supplier;

public class ImageLoadPacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public ImageLoadPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] textureBytes) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public ImageLoadPacket(@NotNull FriendlyByteBuf buf) {
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
            try {
                ClientImages.registerImage(this.dimension, this.pos, NativeImage.read(this.textureBytes), this.textureLocation);
            } catch (IOException ioException) {
                DragIt.LOGGER.error("Error loading image", ioException);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
