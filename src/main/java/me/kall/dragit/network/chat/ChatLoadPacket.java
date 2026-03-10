package me.kall.dragit.network.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ChatLoadPacket extends ChatPacket {
    public ChatLoadPacket(ResourceLocation textureLocation, byte @Nullable [] textureBytes, String sender) {
        super(textureLocation, textureBytes, sender);
    }

    public ChatLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ChatImages.registerChatImage(this.textureLocation, resolved, this.sender));
                if (bytes != null) ChatImages.registerChatImage(this.textureLocation, bytes, this.sender);
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling ChatLoadPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
