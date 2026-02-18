package me.kall.dragit.mixin;

import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.data.ClientTextureData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> cir) {
        ClientTextureData clientTextureData = ClientSkins.SKINS.get(entity.getGameProfile().getId());
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }
}
