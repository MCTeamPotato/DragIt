package me.kall.dragit.network.painting;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.PaintingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintingLoadPacket extends PaintingPacket {
    public PaintingLoadPacket(Identifier dimension, long pos, Identifier textureLocation, byte @Nullable [] textureBytes) {
        super(dimension, pos, textureLocation, textureBytes);
    }

    public PaintingLoadPacket(@NotNull FriendlyByteBuf buf) { super(buf); }

    @Override
    public void handle(ServerPlayer player) {
        try {
            byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientPaintings.registerPainting(this.dimension, this.pos, resolved, this.textureLocation));
            if (bytes != null) ClientPaintings.registerPainting(this.dimension, this.pos, bytes, this.textureLocation);
            DragIt.LOGGER.info("PaintingLoadPacket handled [{}] dim={} pos=[{}]",
                    this.textureLocation, this.dimension,
                    BlockPos.getX(this.pos) + "," + BlockPos.getY(this.pos) + "," + BlockPos.getZ(this.pos));
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling PaintingLoadPacket", t);
        }
    }

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return DragNetworker.PAINTING_LOAD_TYPE;
    }
}
