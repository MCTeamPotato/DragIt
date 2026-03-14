package me.kall.dragit.mixin;

import com.mojang.authlib.GameProfile;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
    @Shadow @Final private GameProfile profile;

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull CallbackInfoReturnable<PlayerSkin> cir) {
        ClientTextureData capeTexture = ClientCapes.CAPES.get(this.profile.id());
        Identifier videoTexture = VideoCapes.getTexture(this.profile.id());
        ClientTextureData skinTexture = ClientSkins.SKINS.get(this.profile.id());

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
