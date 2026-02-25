package me.kall.dragit.network.cache.chat;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ChatHashPacket {
    private static final Component EMPTY = Component.empty();

    public final ResourceLocation textureLocation;
    public final int hash;
    public final String sender;

    public ChatHashPacket(ResourceLocation textureLocation, int hash, String sender) {
        this.textureLocation = textureLocation;
        this.hash = hash;
        this.sender = sender;
    }

    public ChatHashPacket(@NotNull FriendlyByteBuf buf) {
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
                Player sender = ctx.get().getSender();
                List<byte[]> cached = ImageCache.find(this.hash);

                if (sender instanceof ServerPlayer player) {
                    if (cached.size() == 1) {
                        ImageCache.save(cached.get(0));
                        UUID uuid = player.getUUID();
                        List<ServerPlayer> targets = player.server.getPlayerList().getPlayers();
                        if (targets.size() > 1) {
                            for (ServerPlayer target : targets) {
                                if (target.getUUID().equals(uuid)) continue;
                                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new ChatHashPacket(this.textureLocation, this.hash, this.sender));
                            }
                        }
                    } else {
                        DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChatRequestPacket(this.textureLocation, this.hash, this.sender));
                    }
                } else {
                    if (cached.size() == 1) {
                        displayChatImage(cached.get(0));
                    } else {
                        DragNetworker.INSTANCE.sendToServer(new ChatRequestPacket(this.textureLocation, this.hash, this.sender));
                    }
                }
            } catch (Throwable t) {
                DragIt.LOGGER.error("[DragIt] Error handling ChatHashPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void displayChatImage(byte[] bytes) {
        NativeImage image = ChatImages.registerChatImage(this.textureLocation, bytes);
        if (image == null) return;
        ChatComponent chatComponent = Minecraft.getInstance().gui.getChat();
        chatComponent.addMessage(Component.translatable("chat.dragit.shared_image", this.sender));
        chatComponent.addMessage(Component.literal("!image:" + this.textureLocation));
        double width = image.getWidth();
        double height = image.getHeight();
        double scale = Math.min(DragClientConfig.INSTANCE.getChatMaxWidth() / width, DragClientConfig.INSTANCE.getChatMaxHeight() / height);
        for (int i = 0; i < (int) Math.ceil((int) (height * scale) / 9.0) - 1; i++) {
            chatComponent.addMessage(EMPTY);
        }
    }
}