package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void customSkin(@NotNull CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientTextureData capeTexture = ClientCapes.CAPES.get(player.getUUID());
        ClientTextureData skinTexture = ClientSkins.SKINS.get(player.getUUID());

        PlayerSkin currentSkin = cir.getReturnValue();
        if (capeTexture != null) ((PlayerSkinAccessor)(Object)currentSkin).setCapeTexture(capeTexture.textureLocation());
        if (skinTexture != null) ((PlayerSkinAccessor)(Object)currentSkin).setTexture(skinTexture.textureLocation());
        cir.setReturnValue(currentSkin);
    }

}
