package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PaintingPacket extends Handler {
    public final ResourceLocation dimension;
    public final long pos;
    public final ResourceLocation textureLocation;
    public final byte @Nullable [] textureBytes;

    public PaintingPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public PaintingPacket(@NotNull FriendlyByteBuf buf) {
        this.dimension = buf.readResourceLocation();
        this.pos = buf.readLong();
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readBoolean() ? buf.readByteArray() : null;
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.dimension);
        buf.writeLong(this.pos);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeBoolean(this.textureBytes != null);
        if (this.textureBytes != null) buf.writeByteArray(this.textureBytes);
    }
}
