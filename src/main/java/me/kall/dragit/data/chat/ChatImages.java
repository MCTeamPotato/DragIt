package me.kall.dragit.data.chat;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.network.base.ChatPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ChatImages {
    public static final Map<ResourceLocation, DynamicTexture> CHAT_IMAGES = new Object2ObjectOpenHashMap<>();

    public static void registerChatImage(ResourceLocation textureLocation, byte[] textureBytes, String sender) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if (CHAT_IMAGES.containsKey(textureLocation)) {
            textureManager.release(textureLocation);
            CHAT_IMAGES.remove(textureLocation).close();
        }

        try {
            NativeImage nativeImage = NativeImage.read(ByteBuffer.wrap(textureBytes));
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            textureManager.register(textureLocation, dynamicTexture);
            CHAT_IMAGES.put(textureLocation, dynamicTexture);

            ChatComponent chatComponent = Minecraft.getInstance().gui.getChat();
            chatComponent.addMessage(new TranslatableComponent("chat.dragit.shared_image", sender));
            chatComponent.addMessage(new TextComponent("!image:" + textureLocation));

            double maxWidth = DragClientConfig.INSTANCE.getChatMaxWidth();
            double maxHeight = DragClientConfig.INSTANCE.getChatMaxHeight();

            double width = nativeImage.getWidth();
            double height = nativeImage.getHeight();
            double scale = Math.min(maxWidth / width, maxHeight / height);

            int emptyLine = (int) Math.ceil((int) (height * scale) / 9.0) - 1;

            for (int i = 0; i < emptyLine; i++) {
                chatComponent.addMessage(ChatPacket.EMPTY);
            }
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering chat image", exception);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Map.Entry<ResourceLocation, DynamicTexture> entry : CHAT_IMAGES.entrySet()) {
            textureManager.release(entry.getKey());
            entry.getValue().close();
        }
        CHAT_IMAGES.clear();
    }
}
