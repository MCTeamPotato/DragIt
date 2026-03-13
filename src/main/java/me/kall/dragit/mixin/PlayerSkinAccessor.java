package me.kall.dragit.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkin.class)
public interface PlayerSkinAccessor {
    @Mutable
    @Accessor("texture")
    void setTexture(Identifier texture);

    @Mutable
    @Accessor("capeTexture")
    void setCapeTexture(Identifier capeTexture);
}
