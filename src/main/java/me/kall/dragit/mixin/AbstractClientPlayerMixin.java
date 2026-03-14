package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.integration.NarutoLoadingIntegration;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void customSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientTextureData clientTextureData = ClientSkins.SKINS.get(player.getUUID());
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }

    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    private void customCape(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        UUID uuid = player.getUUID();

        if (NarutoLoadingIntegration.isIntegratable()) {
            ResourceLocation videoTex = VideoCapes.getTexture(uuid);
            if (videoTex != null) {
                cir.setReturnValue(videoTex);
                return;
            }
        }

        ClientTextureData capeData = ClientCapes.CAPES.get(uuid);
        if (capeData != null) cir.setReturnValue(capeData.textureLocation());
    }
}
