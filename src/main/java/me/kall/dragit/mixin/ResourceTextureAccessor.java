package me.kall.dragit.mixin;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientAsset.ResourceTexture.class)
public interface ResourceTextureAccessor {
    @Mutable
    @Accessor("id")
    void setId(Identifier identifier);
}