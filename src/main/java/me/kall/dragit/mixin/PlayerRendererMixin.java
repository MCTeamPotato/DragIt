package me.kall.dragit.mixin;

import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> cir) {
        UUID uuid = entity.getGameProfile().getId();
        ClientSkins.Skin skin = ClientSkins.SKINS.get(uuid);
        if (skin != null) cir.setReturnValue(skin.textureLocation());
    }
}
