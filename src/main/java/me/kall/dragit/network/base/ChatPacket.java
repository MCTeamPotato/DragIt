package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ChatPacket {
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public ChatPacket(ResourceLocation textureLocation, byte[] textureBytes) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public ChatPacket(@NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readByteArray());
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
    }

    public abstract void handle(@NotNull Supplier<NetworkEvent.Context> ctx);
}