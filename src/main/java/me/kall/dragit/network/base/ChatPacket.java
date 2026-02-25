package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ChatPacket {
    public static final Component EMPTY = Component.empty();

    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;
    public final String sender;

    public ChatPacket(ResourceLocation textureLocation, byte[] textureBytes, String sender) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
        this.sender = sender;
    }

    public ChatPacket(@NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readByteArray(), buf.readUtf());
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
        buf.writeUtf(this.sender);
    }

    public abstract void handle(@NotNull Supplier<NetworkEvent.Context> ctx);
}