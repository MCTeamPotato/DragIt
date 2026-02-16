package me.kall.dragit.data.skin;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.UUID;

public class ClientSkins {
    public static final Object2ObjectMap<UUID, Skin> SKINS = new Object2ObjectOpenHashMap<>();

    public static void registerSkin(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        try {
            if (SKINS.containsKey(uuid)) {
                Skin removed = SKINS.remove(uuid);
                Minecraft.getInstance().getTextureManager().release(removed.textureLocation());
                removed.dynamicTexture().close();
            }

            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(textureBytes));
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            SKINS.put(uuid, new Skin(textureLocation, dynamicTexture));
            DragIt.LOGGER.info("Skin {} is registered", textureLocation.toString());
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error saving skin", ioException);
        }
    }

    public record Skin(ResourceLocation textureLocation, DynamicTexture dynamicTexture) {}
}
