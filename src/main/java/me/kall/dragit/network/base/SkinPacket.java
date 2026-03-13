package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class SkinPacket extends Handler {
    public final UUID uuid;
    public final Identifier textureLocation;
    public final byte @Nullable [] textureBytes;

    public SkinPacket(UUID uuid, Identifier textureLocation, byte @Nullable [] textureBytes) {
        this.uuid = uuid;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public SkinPacket(@NotNull FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.textureLocation = buf.readIdentifier();
        this.textureBytes = buf.readBoolean() ? buf.readByteArray() : null;
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeIdentifier(this.textureLocation);
        buf.writeBoolean(this.textureBytes != null);
        if (this.textureBytes != null) buf.writeByteArray(this.textureBytes);
    }
}
