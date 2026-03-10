package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.chat.ChatImages;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.chat.ChatSyncPacket;
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
        if (!(minecraft.screen instanceof ChatScreen) || player == null) return false;

        boolean handled = false;
        for (int index = 0; index < count; index++) {
            String filePath = GLFWDropCallback.getName(names, index);
            try {
                String name = player.getName().getString();
                byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getChatMaxPixels());
                ResourceLocation textureLocation = ImageCache.getOrCreate(textureBytes);
                ChatImages.registerChatImage(textureLocation, textureBytes, name);
                if (DragItClient.canSync()) DragNetworker.INSTANCE.sendToServer(new ChatSyncPacket(textureLocation, textureBytes, name));
                handled = true;
            } catch (IOException exception) {
                DragIt.LOGGER.error("Error reading chat image file", exception);
                return false;
            }
        }
        return handled;
    }
}