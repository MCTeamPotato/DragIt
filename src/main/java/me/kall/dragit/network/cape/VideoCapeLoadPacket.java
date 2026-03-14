package me.kall.dragit.network.cape;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.network.base.VideoCapeBasePacket;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public class VideoCapeLoadPacket extends VideoCapeBasePacket {
    public VideoCapeLoadPacket(UUID uuid, String relativePath) {
        super(uuid, relativePath);
    }

    public VideoCapeLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer player) {
        if (this.relativePath == null || this.relativePath.isBlank()) {
            VideoCapes.unregister(this.uuid);
            return;
        }

        String absolutePath = NarutoConfig.absolute(this.relativePath);
        if (absolutePath.isBlank() || !new File(absolutePath).isFile()) {
            DragIt.LOGGER.warn("VideoCapeLoadPacket: video file not found locally, path='{}' (relative='{}')", absolutePath, this.relativePath);
            return;
        }

        VideoCapes.register(this.uuid, absolutePath);
        DragIt.LOGGER.info("VideoCapeLoadPacket: uuid={} path={}", this.uuid, absolutePath);
    }
}