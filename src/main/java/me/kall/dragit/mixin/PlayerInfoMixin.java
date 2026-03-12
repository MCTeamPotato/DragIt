package me.kall.dragit.mixin;

import com.mojang.authlib.GameProfile;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true)
    private void customSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        ClientTextureData clientTextureData = ClientSkins.SKINS.get(this.profile.getId());
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }

    @Inject(method = "getCapeLocation", at = @At("HEAD"), cancellable = true)
    private void customCape(CallbackInfoReturnable<ResourceLocation> cir) {
        ClientTextureData clientTextureData = ClientCapes.CAPES.get(this.profile.getId());
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }
}
