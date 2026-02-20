package me.kall.dragit.data.chat;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public class ChatImages {
    public static final Map<ResourceLocation, DynamicTexture> CHAT_IMAGES = new Object2ObjectOpenHashMap<>();

    public static double MAX_WIDTH = 64;
    public static double MAX_HEIGHT = 64;

    public static @Nullable NativeImage registerChatImage(ResourceLocation textureLocation, byte[] textureBytes) {
        if (CHAT_IMAGES.containsKey(textureLocation)) {
            Minecraft.getInstance().getTextureManager().release(textureLocation);
            CHAT_IMAGES.remove(textureLocation).close();
        }

        try {
            NativeImage nativeImage = NativeImage.read(textureBytes);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            CHAT_IMAGES.put(textureLocation, dynamicTexture);
            return nativeImage;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering chat image", exception);
        }

        return null;
    }
}
