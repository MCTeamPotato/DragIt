package me.kall.dragit.network.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatLoadPacket extends ChatPacket {
    public ChatLoadPacket(Identifier textureLocation, byte @Nullable [] textureBytes, String sender) {
        super(textureLocation, textureBytes, sender);
    }

    public ChatLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer player) {
        try {
            byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ChatImages.registerChatImage(this.textureLocation, resolved, this.sender));
            if (bytes != null) ChatImages.registerChatImage(this.textureLocation, bytes, this.sender);
        } catch (Throwable t) {
            DragIt.LOGGER.error("Error handling ChatLoadPacket", t);
        }
    }

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return DragNetworker.CHAT_LOAD_TYPE;
    }
}
