package me.kall.dragit.network.chat;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ChatLoadPacket extends ChatPacket {
    public ChatLoadPacket(ResourceLocation textureLocation, byte[] textureBytes, String sender) {
        super(textureLocation, textureBytes, sender);
    }

    public ChatLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }


    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ChatImages.registerChatImage(this.textureLocation, this.textureBytes, this.sender);
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling ChatLoadPacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}