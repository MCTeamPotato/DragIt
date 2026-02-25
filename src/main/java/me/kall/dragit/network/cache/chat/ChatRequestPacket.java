package me.kall.dragit.network.cache.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.chat.ChatLoadPacket;
import me.kall.dragit.network.chat.ChatSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ChatRequestPacket {
    public final ResourceLocation textureLocation;
    public final int hash;
    public final String sender;

    public ChatRequestPacket(ResourceLocation textureLocation, int hash, String sender) {
        this.textureLocation = textureLocation;
        this.hash = hash;
        this.sender = sender;
    }

    public ChatRequestPacket(@NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readInt(), buf.readUtf());
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.textureLocation);
        buf.writeInt(this.hash);
        buf.writeUtf(this.sender);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                Player player = ctx.get().getSender();
                List<byte[]> cached = ImageCache.find(this.hash);

                if (player instanceof ServerPlayer serverPlayer) {
                    if (cached.size() == 1) {
                        DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ChatLoadPacket(this.textureLocation, cached.get(0), this.sender));
                    } else {
                        DragIt.LOGGER.warn("[DragIt] Server cannot resolve chat image {} (hash={}), client will miss it", this.textureLocation, this.hash);
                    }
                } else {
                    if (cached.size() == 1) {
                        DragNetworker.INSTANCE.sendToServer(new ChatSyncPacket(this.textureLocation, cached.get(0), this.sender));
                    } else {
                        DragIt.LOGGER.warn("[DragIt] Client cache has no chat image for hash={}, cannot respond to server request", this.hash);
                    }
                }
            } catch (Throwable t) {
                DragIt.LOGGER.error("[DragIt] Error handling ChatRequestPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}