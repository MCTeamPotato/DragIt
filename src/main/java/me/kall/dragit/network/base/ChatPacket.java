package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ChatPacket extends Handler {
    public static final Component EMPTY = Component.empty();

    public final Identifier textureLocation;
    public final byte @Nullable [] textureBytes;
    public final String sender;

    public ChatPacket(Identifier textureLocation, byte @Nullable [] textureBytes, String sender) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
        this.sender = sender;
    }

    public ChatPacket(@NotNull FriendlyByteBuf buf) {
        this.textureLocation = buf.readIdentifier();
        this.textureBytes = buf.readBoolean() ? buf.readByteArray() : null;
        this.sender = buf.readUtf();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeIdentifier(this.textureLocation);
        buf.writeBoolean(this.textureBytes != null);
        if (this.textureBytes != null) buf.writeByteArray(this.textureBytes);
        buf.writeUtf(this.sender);
    }
}
