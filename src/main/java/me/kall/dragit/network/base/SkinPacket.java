package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class SkinPacket {
    public final UUID uuid;
    public final ResourceLocation textureLocation;
    public final byte @Nullable [] textureBytes;

    public SkinPacket(UUID uuid, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        this.uuid = uuid;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public SkinPacket(@NotNull FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readBoolean() ? buf.readByteArray() : null;
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeBoolean(this.textureBytes != null);
        if (this.textureBytes != null) buf.writeByteArray(this.textureBytes);
    }

    public abstract void handle(@NotNull Supplier<NetworkEvent.Context> ctx);
}
