package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;

public class ChatDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.screen instanceof ChatScreen && player != null) {
            boolean handled = false;
            for (int index = 0; index < count; index++) {
                String filePath = GLFWDropCallback.getName(names, index);
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_" + System.currentTimeMillis() + "_" + index);
                try {
                    byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getChatMaxPixels());
                    ChatImages.registerChatImage(textureLocation, textureBytes, player.getName().getString());
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