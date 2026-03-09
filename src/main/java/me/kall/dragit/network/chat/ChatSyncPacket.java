package me.kall.dragit.network.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ChatSyncPacket extends ChatPacket {
    public ChatSyncPacket(ResourceLocation textureLocation, byte[] textureBytes, String sender) {
        super(textureLocation, textureBytes, sender);
    }

    public ChatSyncPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
                if (syncTargets.size() == 1) return;
                UUID uuid = player.getUUID();
                for (ServerPlayer syncTarget : syncTargets) {
                    if (syncTarget.getUUID().equals(uuid)) continue;
                    DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new ChatLoadPacket(this.textureLocation, this.textureBytes, this.sender));
                }
                DragIt.LOGGER.info("ChatSyncPacket handled. TextureLocation: {}. Sender: {}", this.textureLocation.toString(), this.sender);
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling ChatSavePacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}