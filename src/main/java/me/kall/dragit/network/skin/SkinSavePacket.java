package me.kall.dragit.network.skin;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.data.skin.SavedSkins;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SkinSavePacket extends SkinPacket {
    public SkinSavePacket(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public SkinSavePacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(ServerPlayer player) {
        try {
            if (player == null || this.textureBytes == null) return;

            ImageCache.store(this.textureLocation, this.textureBytes);

            SavedSkins savedSkins = SavedSkins.get(player.getLevel());
            savedSkins.setDirty();
            savedSkins.skins.put(this.uuid, new SavedTextureData(this.textureLocation, this.textureBytes));

            List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
            if (syncTargets.size() == 1) return;
            UUID senderUUID = player.getUUID();
            for (ServerPlayer syncTarget : syncTargets) {
                if (syncTarget.getUUID().equals(senderUUID)) continue;
                DragNetworker.send(syncTarget, new SkinLoadPacket(this.uuid, this.textureLocation, this.textureBytes));
            }
            DragIt.LOGGER.info("SkinSavePacket handled [{}] uuid={}", this.textureLocation, this.uuid);
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling SkinSavePacket", t);
        }
    }
}
