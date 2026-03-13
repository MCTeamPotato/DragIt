package me.kall.dragit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin<T extends ItemFrame> {
    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/ItemFrame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
    private void renderItemFrameImage(@NotNull T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        ClientItemFrames.FrameEntry frameEntry = ClientItemFrames.getFrame(entity.level().dimension().identifier(), entity.blockPosition().asLong());
        if (frameEntry == null) return;

        float uMin = (float) frameEntry.column() / frameEntry.totalColumns();
        float uMax = (float) (frameEntry.column() + 1) / frameEntry.totalColumns();
        float vMin = (float) frameEntry.row() / frameEntry.totalRows();
        float vMax = (float) (frameEntry.row() + 1) / frameEntry.totalRows();

        int lightColor = LevelRenderer.getLightColor(entity.level(), entity.blockPosition());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entitySolid(frameEntry.textureLocation()));
        PoseStack.Pose pose = poseStack.last();

        this.dragIt$vertex(vertexConsumer, pose,  0.5F, -0.5F, 0.4F, uMin, vMax, lightColor);
        this.dragIt$vertex(vertexConsumer, pose, -0.5F, -0.5F, 0.4F, uMax, vMax, lightColor);
        this.dragIt$vertex(vertexConsumer, pose, -0.5F,  0.5F, 0.4F, uMax, vMin, lightColor);
        this.dragIt$vertex(vertexConsumer, pose,  0.5F,  0.5F, 0.4F, uMin, vMin, lightColor);
    }

    @Unique
    private void dragIt$vertex(@NotNull VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int lightColor) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightColor).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}