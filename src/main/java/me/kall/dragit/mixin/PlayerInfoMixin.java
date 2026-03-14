package me.kall.dragit.mixin;

import com.mojang.authlib.GameProfile;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
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
        ClientTextureData capeTexture = ClientCapes.CAPES.get(this.profile.getId());
        ResourceLocation videoTexture = VideoCapes.getTexture(this.profile.getId());
        ClientTextureData skinTexture = ClientSkins.SKINS.get(this.profile.getId());
        PlayerSkin currentSkin = cir.getReturnValue();
        if (capeTexture != null) ((PlayerSkinAccessor)(Object)currentSkin).setCapeTexture(capeTexture.textureLocation());
        if (videoTexture != null) ((PlayerSkinAccessor)(Object)currentSkin).setCapeTexture(videoTexture);
        if (skinTexture != null) ((PlayerSkinAccessor)(Object)currentSkin).setTexture(skinTexture.textureLocation());
        cir.setReturnValue(currentSkin);
    }
}
