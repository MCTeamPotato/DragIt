package me.kall.dragit.data.cape;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.ClientTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.io.IOException;
import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT, modid = DragIt.MOD_ID)
public class ClientCapes {
    public static final Object2ObjectMap<UUID, ClientTextureData> CAPES = new Object2ObjectOpenHashMap<>();

    public static void registerCape(UUID uuid, Identifier textureLocation, byte[] textureBytes) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        try {
            if (CAPES.containsKey(uuid)) {
                ClientTextureData removed = CAPES.remove(uuid);
                textureManager.release(removed.textureLocation());
                removed.dynamicTexture().close();
            }

            DynamicTexture dynamicTexture = new DynamicTexture(textureLocation::toString, NativeImage.read(textureBytes));
            textureManager.register(textureLocation, dynamicTexture);
            CAPES.put(uuid, new ClientTextureData(textureLocation, dynamicTexture));
            DragIt.LOGGER.info("Cape {} is registered", textureLocation.toString());
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error registering cape", ioException);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggingOut event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ClientTextureData clientTextureData : CAPES.values()) {
            textureManager.release(clientTextureData.textureLocation());
            clientTextureData.dynamicTexture().close();
        }
        CAPES.clear();
    }
}
