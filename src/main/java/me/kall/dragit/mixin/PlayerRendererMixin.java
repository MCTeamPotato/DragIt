package me.kall.dragit.mixin;

import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void customSkin(AvatarRenderState avatarRenderState, CallbackInfoReturnable<Identifier> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        ClientTextureData clientTextureData = null;
        if (level != null) {
            Entity entity = level.getEntity(avatarRenderState.id);
            if (entity != null) clientTextureData = ClientSkins.SKINS.get(entity.getUUID());
        }
        if (clientTextureData != null) cir.setReturnValue(clientTextureData.textureLocation());
    }
}
