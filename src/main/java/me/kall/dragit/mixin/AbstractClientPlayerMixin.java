package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At("RETURN"), cancellable = true)
    private void customSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientTextureData clientTextureData = ClientSkins.SKINS.get(player.getUUID());
        if (clientTextureData != null) {
            PlayerSkin skin = cir.getReturnValue();
            ClientAsset.Texture texture = skin.body();
            if (texture instanceof ClientAsset.DownloadedTexture) {
                ((DownloadedTextureAccessor) texture).setTexturePath(clientTextureData.textureLocation());
            } else if (texture instanceof ClientAsset.ResourceTexture) {
                ((ResourceTextureAccessor) texture).setId(clientTextureData.textureLocation());
            }
            ((PlayerSkinAccessor) (Object) skin).setBody(texture);
            cir.setReturnValue(skin);
        }
    }
}
