package me.kall.dragit.network.cape;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.data.cape.SavedCapes;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CapeSavePacket extends SkinPacket {
    public CapeSavePacket(UUID uuid, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public CapeSavePacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(ServerPlayer player) {
        try {
            if (player == null || this.textureBytes == null) return;

            ImageCache.store(this.textureLocation, this.textureBytes);

            SavedCapes savedCapes = SavedCapes.get(player.getLevel());
            savedCapes.setDirty();
            savedCapes.capes.put(this.uuid, new SavedTextureData(this.textureLocation, this.textureBytes));

            List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
            if (syncTargets.size() == 1) return;
            UUID senderUUID = player.getUUID();
            for (ServerPlayer syncTarget : syncTargets) {
                if (syncTarget.getUUID().equals(senderUUID)) continue;
                DragNetworker.send(syncTarget, new CapeLoadPacket(this.uuid, this.textureLocation, this.textureBytes));
            }
            DragIt.LOGGER.info("CapeSavePacket handled [{}] uuid={}", this.textureLocation, this.uuid);
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling CapeSavePacket", t);
        }
    }
}
