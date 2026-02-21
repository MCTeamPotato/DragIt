package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void customSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientTextureData clientTextureData = ClientSkins.SKINS.get(player.getUUID());
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }
}
