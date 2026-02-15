package me.kall.dragit.network.painting;

import me.kall.dragit.data.painting.ClientPaintings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PaintingLoadPacket {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public PaintingLoadPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] textureBytes) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public PaintingLoadPacket(@NotNull FriendlyByteBuf buf) {
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
        ctx.get().enqueueWork(() -> ClientPaintings.registerPainting(this.dimension, this.pos, this.textureBytes, this.textureLocation));
        ctx.get().setPacketHandled(true);
    }
}
