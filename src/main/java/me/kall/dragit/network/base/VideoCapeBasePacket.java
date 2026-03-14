package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class VideoCapeBasePacket extends Handler {
    public final UUID uuid;
    public final String relativePath;

    protected VideoCapeBasePacket(UUID uuid, String relativePath) {
        this.uuid = uuid;
        this.relativePath = relativePath;
    }

    protected VideoCapeBasePacket(@NotNull FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.relativePath = buf.readUtf();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeUtf(this.relativePath);
    }
}