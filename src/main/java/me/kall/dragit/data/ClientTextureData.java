package me.kall.dragit.data;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("ClassCanBeRecord")
public class ClientTextureData {
    private final ResourceLocation textureLocation;
    private final DynamicTexture dynamicTexture;

    public ClientTextureData(ResourceLocation textureLocation, DynamicTexture dynamicTexture) {
        this.textureLocation = textureLocation;
        this.dynamicTexture = dynamicTexture;
    }

    public ResourceLocation textureLocation() {
        return this.textureLocation;
    }

    public DynamicTexture dynamicTexture() {
        return this.dynamicTexture;
    }
}