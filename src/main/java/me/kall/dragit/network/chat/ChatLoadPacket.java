package me.kall.dragit.network.chat;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ChatLoadPacket extends ChatPacket {
    private static final Component EMPTY = Component.empty();

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
                NativeImage image = ChatImages.registerChatImage(this.textureLocation, this.textureBytes);
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
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling ChatLoadPacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}