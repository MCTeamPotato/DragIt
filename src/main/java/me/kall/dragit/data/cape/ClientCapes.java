package me.kall.dragit.data.cape;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.data.ClientTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DragIt.MOD_ID)
public class ClientCapes {
    public static final Object2ObjectMap<UUID, ClientTextureData> CAPES = new Object2ObjectOpenHashMap<>();

    public static void registerCape(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        try {
            if (CAPES.containsKey(uuid)) {
                ClientTextureData removed = CAPES.remove(uuid);
                textureManager.release(removed.textureLocation());
                removed.dynamicTexture().close();
            }

            DynamicTexture dynamicTexture = new DynamicTexture(DragItClient.buildImage(textureBytes));
            textureManager.register(textureLocation, dynamicTexture);
            CAPES.put(uuid, new ClientTextureData(textureLocation, dynamicTexture));
            DragIt.LOGGER.info("Cape {} is registered", textureLocation.toString());
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error registering cape", ioException);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ClientTextureData clientTextureData : CAPES.values()) {
            textureManager.release(clientTextureData.textureLocation());
            clientTextureData.dynamicTexture().close();
        }
        CAPES.clear();
    }
}
