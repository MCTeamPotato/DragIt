package me.kall.dragit.mixin;

import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkin.class)
public interface PlayerSkinAccessor {
    @Mutable
    @Accessor("body")
    void setBody(ClientAsset.Texture body);

    @Mutable
    @Accessor("cape")
    void setCape(ClientAsset.Texture cape);
}