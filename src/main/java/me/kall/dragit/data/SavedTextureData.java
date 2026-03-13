package me.kall.dragit.data;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("ClassCanBeRecord")
public class SavedTextureData {
    private final ResourceLocation textureLocation;
    private final byte[] textureBytes;

    public SavedTextureData(ResourceLocation textureLocation, byte[] textureBytes) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public ResourceLocation textureLocation() {
        return this.textureLocation;
    }

    public byte[] textureBytes() {
        return this.textureBytes;
    }
}