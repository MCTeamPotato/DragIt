package me.kall.dragit.callback.invoker;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.chat.ChatSyncPacket;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;

public class ChatDropInvoker implements DragCallback.Invoker {
    private static final Component EMPTY = Component.empty();

    @Override
    public boolean invoke(long window, int count, long names) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.screen instanceof ChatScreen && player != null) {
            ChatComponent chatComponent = minecraft.gui.getChat();
            String name = player.getName().getString();
            boolean handled = false;
            for (int index = 0; index < count; index++) {
                String filePath = GLFWDropCallback.getName(names, index);
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_" + System.currentTimeMillis() + "_" + index);
                try {
                    byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getChatMaxPixels());
                    NativeImage image = ChatImages.registerChatImage(textureLocation, textureBytes);
                    if (image != null) {
                        chatComponent.addMessage(Component.translatable("chat.dragit.shared_image", name));
                        chatComponent.addMessage(Component.literal("!image:" + textureLocation));
                        double width = image.getWidth();
                        double height = image.getHeight();
                        double scale = Math.min(DragClientConfig.INSTANCE.getChatMaxWidth() / width, DragClientConfig.INSTANCE.getChatMaxHeight() / height);

                        for (int i = 0; i < (int) Math.ceil((int) (height * scale) / 9.0) - 1; i++) {
                            chatComponent.addMessage(EMPTY);
                        }

                        if (DragItClient.canSync()) DragNetworker.INSTANCE.sendToServer(new ChatSyncPacket(textureLocation, textureBytes, name));
                    }

                    handled = true;
                } catch (IOException exception) {
                    DragIt.LOGGER.error("Error reading chat image file", exception);
                    return false;
                }
            }

            return handled;
        }

        return false;
    }
}