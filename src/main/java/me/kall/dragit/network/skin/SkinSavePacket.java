package me.kall.dragit.network.skin;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.SavedSkins;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SkinSavePacket extends SkinPacket {
    public SkinSavePacket(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public SkinSavePacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                SavedSkins savedSkins = SavedSkins.get(player.serverLevel());
                savedSkins.setDirty();
                savedSkins.skins.put(this.uuid, new SavedTextureData(this.textureLocation, this.textureBytes));

                List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
                if (syncTargets.size() == 1) return;
                UUID uuid = player.getUUID();
                for (ServerPlayer syncTarget : syncTargets) {
                    if (syncTarget.getUUID().equals(uuid)) continue;
                    DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new SkinLoadPacket(this.uuid, this.textureLocation, this.textureBytes));
                }
                DragIt.LOGGER.info("PaintingLoadPacket handled. TextureLocation: {}. UUID: {}.", this.textureLocation.toString(), this.uuid.toString());
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling SkinSavePacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
