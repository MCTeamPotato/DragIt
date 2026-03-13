package me.kall.dragit.data;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public class ClientTextureData {
    private final Identifier textureLocation;
    private final DynamicTexture dynamicTexture;

    public ClientTextureData(Identifier textureLocation, DynamicTexture dynamicTexture) {
        this.textureLocation = textureLocation;
        this.dynamicTexture = dynamicTexture;
    }

    public Identifier textureLocation() {
        return this.textureLocation;
    }

    public DynamicTexture dynamicTexture() {
        return this.dynamicTexture;
    }
}