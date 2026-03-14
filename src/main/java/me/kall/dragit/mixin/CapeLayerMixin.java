package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import me.kall.dragit.integration.NarutoLoadingIntegration;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PlayerModel;renderCloak(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void wrapRenderCloak(PlayerModel<?> model, PoseStack poseStack, VertexConsumer ignored, int packedLight, int packedOverlay, Operation<Void> original, @Local(argsOnly = true) @NotNull AbstractClientPlayer player, @Local(argsOnly = true) MultiBufferSource buffer) {
        UUID uuid = player.getUUID();

        if (NarutoLoadingIntegration.isIntegratable()) {
            VideoCapes.VideoCape videoEntry = VideoCapes.VIDEO_CAPES.get(uuid);
            if (videoEntry != null) {
                this.dragIt$renderCapeQuad(buffer, poseStack, videoEntry.location(), packedLight);
                return;
            }
        }

        ClientTextureData cape = ClientCapes.CAPES.get(uuid);
        if (cape != null) {
            this.dragIt$renderCapeQuad(buffer, poseStack, cape.textureLocation(), packedLight);
            return;
        }

        original.call(model, poseStack, ignored, packedLight, packedOverlay);
    }
    @Unique
    private void dragIt$renderCapeQuad(@NotNull MultiBufferSource buffer, @NotNull PoseStack poseStack, ResourceLocation texture, int packedLight) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        Matrix4f pose = poseStack.last().pose();
        vertexConsumer.addVertex(pose, -0.3125F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(pose, -0.3125F, 1.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(pose, +0.3125F, 1.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(pose, +0.3125F, 0.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
    }
}