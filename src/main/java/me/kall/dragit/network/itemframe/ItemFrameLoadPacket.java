package me.kall.dragit.network.itemframe;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.ItemFramePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFrameLoadPacket extends ItemFramePacket {
    public ItemFrameLoadPacket(Identifier dimension, Identifier textureLocation, byte @Nullable [] textureBytes, long[] positions, int[] columns, int[] rows, int totalColumns, int totalRows) {
        super(dimension, textureLocation, textureBytes, positions, columns, rows, totalColumns, totalRows);
    }

    public ItemFrameLoadPacket(@NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void handle(ServerPlayer player) {
        try {
            byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientItemFrames.registerGroup(this.dimension, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows, this.textureLocation, resolved));
            if (bytes != null) ClientItemFrames.registerGroup(this.dimension, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows, this.textureLocation, bytes);
            DragIt.LOGGER.info("ItemFrameLoadPacket handled [{}] dim={}", this.textureLocation, this.dimension);
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling ItemFrameLoadPacket", t);
        }
    }

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return DragNetworker.ITEM_FRAME_LOAD_TYPE;
    }
}
