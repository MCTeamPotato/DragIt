package me.kall.dragit.network.skin;

import me.kall.dragit.data.skin.SavedSkins;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SkinSavePacket {
    public final UUID uuid;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public SkinSavePacket(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        this.uuid = uuid;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public SkinSavePacket(@NotNull FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readByteArray();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SavedSkins savedSkins = SavedSkins.get(player.serverLevel());
            savedSkins.setDirty();
            savedSkins.skins.put(this.uuid, new SavedSkins.Skin(this.textureLocation, this.textureBytes));

            List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
            if (syncTargets.size() == 1) return;
            UUID uuid = player.getUUID();
            for (ServerPlayer syncTarget : syncTargets) {
                if (syncTarget.getUUID().equals(uuid)) continue;
                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new SkinLoadPacket(this.uuid, this.textureLocation, this.textureBytes));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
