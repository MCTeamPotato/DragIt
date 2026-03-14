package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientTextureData capeTexture = ClientCapes.CAPES.get(player.getUUID());
        Identifier videoTexture = VideoCapes.getTexture(player.getUUID());
        ClientTextureData skinTexture = ClientSkins.SKINS.get(player.getUUID());

        PlayerSkin currentSkin = cir.getReturnValue();

        ClientAsset.Texture texture = currentSkin.body();
        if (skinTexture != null) {
            if (texture instanceof ClientAsset.DownloadedTexture) {
                ((DownloadedTextureAccessor) texture).setTexturePath(skinTexture.textureLocation());
            } else if (texture instanceof ClientAsset.ResourceTexture) {
                ((ResourceTextureAccessor) texture).setId(skinTexture.textureLocation());
            }
            ((PlayerSkinAccessor)(Object)currentSkin).setBody(texture);
        }

        Identifier capeTex = videoTexture != null ? videoTexture : capeTexture != null ? capeTexture.textureLocation() : null;

        ClientAsset.Texture cape = currentSkin.cape();
        if (capeTex != null){
            if (cape instanceof ClientAsset.DownloadedTexture) {
                ((DownloadedTextureAccessor) cape).setTexturePath(capeTex);
            } else if (cape instanceof ClientAsset.ResourceTexture) {
                ((ResourceTextureAccessor)cape).setId(capeTex);
            }
            ((PlayerSkinAccessor)(Object)currentSkin).setCape(cape);
        }
        cir.setReturnValue(currentSkin);
    }
}
