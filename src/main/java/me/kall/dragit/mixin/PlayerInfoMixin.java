package me.kall.dragit.mixin;

import com.mojang.authlib.GameProfile;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
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

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull CallbackInfoReturnable<PlayerSkin> cir) {
        ClientTextureData skinTexture = ClientSkins.SKINS.get(this.profile.id());
        if (skinTexture != null) {
            PlayerSkin currentSkin = cir.getReturnValue();
            ClientAsset.Texture texture = currentSkin.body();
            if (texture instanceof ClientAsset.DownloadedTexture) {
                ((DownloadedTextureAccessor) texture).setTexturePath(skinTexture.textureLocation());
            } else if (texture instanceof ClientAsset.ResourceTexture) {
                ((ResourceTextureAccessor) texture).setId(skinTexture.textureLocation());
            }
            ((PlayerSkinAccessor) (Object) currentSkin).setBody(texture);
            cir.setReturnValue(currentSkin);
        }
    }
}
