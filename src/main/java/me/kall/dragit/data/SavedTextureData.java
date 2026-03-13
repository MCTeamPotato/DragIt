package me.kall.dragit.data;

import net.minecraft.resources.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public class SavedTextureData {
    private final Identifier textureLocation;
    private final byte[] textureBytes;

    public SavedTextureData(Identifier textureLocation, byte[] textureBytes) {
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public Identifier textureLocation() {
        return this.textureLocation;
    }

    public byte[] textureBytes() {
        return this.textureBytes;
    }
}