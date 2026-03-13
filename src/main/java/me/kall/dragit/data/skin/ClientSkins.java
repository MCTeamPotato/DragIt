package me.kall.dragit.data.skin;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
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
import java.nio.ByteBuffer;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DragIt.MOD_ID)
public class ClientSkins {
    public static final Object2ObjectMap<UUID, ClientTextureData> SKINS = new Object2ObjectOpenHashMap<>();

    public static void registerSkin(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        try {
            if (SKINS.containsKey(uuid)) {
                ClientTextureData removed = SKINS.remove(uuid);
                textureManager.release(removed.textureLocation());
                removed.dynamicTexture().close();
            }

            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(ByteBuffer.wrap(textureBytes)));
            textureManager.register(textureLocation, dynamicTexture);
            SKINS.put(uuid, new ClientTextureData(textureLocation, dynamicTexture));
            DragIt.LOGGER.info("Skin {} is registered", textureLocation.toString());
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error registering skin", ioException);
        }
    }

    @SubscribeEvent
    public static void clearImages(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ClientTextureData clientTextureData : SKINS.values()) {
            textureManager.release(clientTextureData.textureLocation());
            clientTextureData.dynamicTexture().close();
        }
        SKINS.clear();
    }
}
