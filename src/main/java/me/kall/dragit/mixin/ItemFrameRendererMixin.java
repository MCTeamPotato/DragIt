package me.kall.dragit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
    private void renderItemFrameImage(ItemFrameRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Identifier dimension = level.dimension().identifier();
        long position = BlockPos.asLong(Mth.floor(state.x), Mth.floor(state.y), Mth.floor(state.z));

        ClientItemFrames.FrameEntry frameEntry = ClientItemFrames.getFrame(dimension, position);
        if (frameEntry == null) return;

        float uMin = (float) frameEntry.column() / frameEntry.totalColumns();
        float uMax = (float) (frameEntry.column() + 1) / frameEntry.totalColumns();
        float vMin = (float) frameEntry.row() / frameEntry.totalRows();
        float vMax = (float) (frameEntry.row() + 1) / frameEntry.totalRows();

        int lightColor = state.lightCoords;

        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(frameEntry.textureLocation()), (pose, consumer) -> {
            dragIt$vertex(consumer, pose,  0.5F, -0.5F, 0.4F, uMin, vMax, lightColor);
            dragIt$vertex(consumer, pose, -0.5F, -0.5F, 0.4F, uMax, vMax, lightColor);
            dragIt$vertex(consumer, pose, -0.5F,  0.5F, 0.4F, uMax, vMin, lightColor);
            dragIt$vertex(consumer, pose,  0.5F,  0.5F, 0.4F, uMin, vMin, lightColor);
        });
    }

    @Unique
    @SuppressWarnings("SameParameterValue")
    private static void dragIt$vertex(@NotNull VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int lightColor) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightColor).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}