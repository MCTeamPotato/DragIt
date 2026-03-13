package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PaintingPacket extends Handler {
    public final Identifier dimension;
    public final long pos;
    public final Identifier textureLocation;
    public final byte @Nullable [] textureBytes;

    public PaintingPacket(Identifier dimension, long pos, Identifier textureLocation, byte @Nullable [] textureBytes) {
        this.dimension = dimension;
        this.pos = pos;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public PaintingPacket(@NotNull FriendlyByteBuf buf) {
        this.dimension = buf.readIdentifier();
        this.pos = buf.readLong();
        this.textureLocation = buf.readIdentifier();
        this.textureBytes = buf.readBoolean() ? buf.readByteArray() : null;
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeIdentifier(this.dimension);
        buf.writeLong(this.pos);
        buf.writeIdentifier(this.textureLocation);
        buf.writeBoolean(this.textureBytes != null);
        if (this.textureBytes != null) buf.writeByteArray(this.textureBytes);
    }
}
