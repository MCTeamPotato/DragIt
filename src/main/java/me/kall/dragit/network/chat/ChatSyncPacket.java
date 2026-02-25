package me.kall.dragit.network.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.ChatPacket;
import me.kall.dragit.network.cache.chat.ChatHashPacket;
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

                ImageCache.save(this.textureBytes);

                List<ServerPlayer> syncTargets = player.server.getPlayerList().getPlayers();
                if (syncTargets.size() == 1) return;

                UUID uuid = player.getUUID();
                int hash = ImageCache.hash(this.textureBytes);
                for (ServerPlayer syncTarget : syncTargets) {
                    if (syncTarget.getUUID().equals(uuid)) continue;
                    DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTarget), new ChatHashPacket(this.textureLocation, hash, this.sender));
                }
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling ChatSyncPacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}