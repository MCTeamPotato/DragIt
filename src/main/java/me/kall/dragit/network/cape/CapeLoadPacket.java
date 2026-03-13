package me.kall.dragit.network.cape;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CapeLoadPacket extends SkinPacket {
    public CapeLoadPacket(UUID uuid, Identifier textureLocation, byte @Nullable [] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public CapeLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(ServerPlayer player) {
        try {
            byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientCapes.registerCape(this.uuid, this.textureLocation, resolved));
            if (bytes != null) ClientCapes.registerCape(this.uuid, this.textureLocation, bytes);
            DragIt.LOGGER.info("CapeLoadPacket handled [{}] uuid={}", this.textureLocation, this.uuid);
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling CapeLoadPacket", t);
        }
    }

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return DragNetworker.CAPE_LOAD_TYPE;
    }
}
