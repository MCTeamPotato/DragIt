package me.kall.dragit.data.painting;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.ClientTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
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
            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(textureBytes));
            textureManager.register(textureLocation, dynamicTexture);
            PAINTINGS.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>()).put(pos, new ClientTextureData(textureLocation, dynamicTexture));
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error registering painting", ioException);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggingOut event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Long2ObjectMap<ClientTextureData> imageMap : PAINTINGS.values()) {
            for (ClientTextureData clientTextureData : imageMap.values()) {
                textureManager.release(clientTextureData.textureLocation());
                clientTextureData.dynamicTexture().close();
            }
        }
        PAINTINGS.clear();
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Painting painting && painting.level() instanceof ClientLevel level) {
            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();
            Entity.RemovalReason removalReason = painting.getRemovalReason();
            if (removalReason != Entity.RemovalReason.KILLED && removalReason != Entity.RemovalReason.DISCARDED) return;

            Long2ObjectMap<ClientTextureData> imageMap = PAINTINGS.get(dimension);
            if (imageMap == null) return;
            if (imageMap.containsKey(pos)) {
                ClientTextureData image = imageMap.get(pos);
                image.dynamicTexture().close();
                Minecraft.getInstance().getTextureManager().release(image.textureLocation());
                imageMap.remove(pos);
            }
            if (imageMap.isEmpty()) PAINTINGS.remove(dimension);
        }
    }
}
