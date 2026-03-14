package me.kall.dragit.network.cape;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.cape.SavedVideoCapes;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.VideoCapeBasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class VideoCapeSavePacket extends VideoCapeBasePacket {
    public VideoCapeSavePacket(UUID uuid, String relativePath) {
        super(uuid, relativePath);
    }

    public VideoCapeSavePacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null) return;

        SavedVideoCapes data = SavedVideoCapes.get(sender.serverLevel());
        if (this.relativePath == null || this.relativePath.isBlank()) {
            data.videoCapes.remove(this.uuid);
        } else {
            data.videoCapes.put(this.uuid, this.relativePath);
        }
        data.setDirty();

        UUID senderUUID = sender.getUUID();
        for (ServerPlayer other : sender.server.getPlayerList().getPlayers()) {
            if (!other.getUUID().equals(senderUUID)) {
                DragNetworker.send(other, new VideoCapeLoadPacket(this.uuid, this.relativePath));
            }
        }

        DragIt.LOGGER.info("VideoCapeSavePacket: uuid={} path={} saved and broadcasted", this.uuid, this.relativePath);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return DragNetworker.VIDEO_CAPE_SAVE_TYPE;
    }
}