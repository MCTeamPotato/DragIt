package me.kall.dragit.data.chat;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ChatImages {
    public static final Map<ResourceLocation, DynamicTexture> CHAT_IMAGES = new Object2ObjectOpenHashMap<>();

    public static @Nullable NativeImage registerChatImage(ResourceLocation textureLocation, byte[] textureBytes) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if (CHAT_IMAGES.containsKey(textureLocation)) {
            textureManager.release(textureLocation);
            CHAT_IMAGES.remove(textureLocation).close();
        }

        try {
            NativeImage nativeImage = NativeImage.read(textureBytes);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            textureManager.register(textureLocation, dynamicTexture);
            CHAT_IMAGES.put(textureLocation, dynamicTexture);
            return nativeImage;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering chat image", exception);
        }

        return null;
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggingOut event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Map.Entry<ResourceLocation, DynamicTexture> entry : CHAT_IMAGES.entrySet()) {
            textureManager.release(entry.getKey());
            entry.getValue().close();
        }
        CHAT_IMAGES.clear();
    }
}
