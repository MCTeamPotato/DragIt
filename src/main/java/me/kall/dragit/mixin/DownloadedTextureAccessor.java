package me.kall.dragit.mixin;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientAsset.DownloadedTexture.class)
public interface DownloadedTextureAccessor {
    @Mutable
    @Accessor("texturePath")
    void setTexturePath(Identifier identifier);
}