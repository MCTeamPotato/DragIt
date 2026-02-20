package me.kall.dragit.callback.invoker;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.data.chat.ChatImages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.FileInputStream;
import java.io.IOException;

public class ChatDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ChatScreen) {
            ChatComponent chatComponent = minecraft.gui.getChat();
            for (int index = 0; index < count; index++) {
                String filePath = GLFWDropCallback.getName(names, index);
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_" + System.currentTimeMillis() + "_" + index);
                try (FileInputStream imageFile = new FileInputStream(filePath)) {
                    byte[] textureBytes = imageFile.readAllBytes();
                    NativeImage image = ChatImages.registerChatImage(textureLocation, textureBytes);
                    if (image != null) {
                        chatComponent.addMessage(Component.literal("!image:" + textureLocation));
                        double width = image.getWidth();
                        double height = image.getHeight();
                        double scale = Math.min(ChatImages.MAX_WIDTH / width, ChatImages.MAX_HEIGHT / height);

                        for (int i = 0; i < (int) Math.ceil((int)(height * scale) / 9.0) - 1; i++) {
                            chatComponent.addMessage(Component.empty());
                        }
                    }
                } catch (IOException exception) {
                    DragIt.LOGGER.error("Error reading image file", exception);
                    return false;
                }
            }
        }

        return false;
    }
}
