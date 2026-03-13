package me.kall.dragit.data.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ClientPaintings {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<ClientTextureData>> PAINTINGS = new Object2ObjectOpenHashMap<>();

    public static void registerPainting(ResourceLocation dimension, long pos, byte[] textureBytes, ResourceLocation textureLocation) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if (PAINTINGS.containsKey(dimension)) {
            Long2ObjectMap<ClientTextureData> positionMap = PAINTINGS.get(dimension);
            if (positionMap.containsKey(pos)) {
                ClientTextureData removed = positionMap.remove(pos);
                textureManager.release(removed.textureLocation());
                removed.dynamicTexture().close();
                if (positionMap.isEmpty()) PAINTINGS.remove(dimension);
            }
        }

        try {
            DynamicTexture dynamicTexture = new DynamicTexture(DragItClient.buildImage(textureBytes));
            textureManager.register(textureLocation, dynamicTexture);
            PAINTINGS.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>()).put(pos, new ClientTextureData(textureLocation, dynamicTexture));
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error registering painting", ioException);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Long2ObjectMap<ClientTextureData> imageMap : PAINTINGS.values()) {
            for (ClientTextureData clientTextureData : imageMap.values()) {
                textureManager.release(clientTextureData.textureLocation());
                clientTextureData.dynamicTexture().close();
            }
        }
        PAINTINGS.clear();
    }
}
