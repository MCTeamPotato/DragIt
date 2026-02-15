package me.kall.dragit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.kall.dragit.data.painting.ClientPaintings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin extends EntityRenderer<Painting> {
    protected PaintingRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/Painting;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void renderDropped(@NotNull Painting painting, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        ResourceLocation dimension = painting.level().dimension().location();
        long pos = painting.blockPosition().asLong();
        Long2ObjectMap<ClientPaintings.Painting> imageMap = ClientPaintings.PAINTINGS.get(dimension);
        if (imageMap == null) return;
        ClientPaintings.Painting image = imageMap.get(pos);
        if (image == null) return;
        ci.cancel();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);

        PaintingVariant paintingVariant = painting.getVariant().value();
        int width = paintingVariant.getWidth();
        int height = paintingVariant.getHeight();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        float minX = -width / 2f;
        float minY = -height / 2f;
        int tilesX = width / 16;
        int tilesY = height / 16;

        VertexConsumer imageConsumer = buffer.getBuffer(RenderType.entitySolid(image.textureLocation()));
        int light = LevelRenderer.getLightColor(painting.level(), painting.blockPosition());

        for (int xTile = 0; xTile < tilesX; xTile++) {
            for (int yTile = 0; yTile < tilesY; yTile++) {
                float x0 = minX + xTile * 16;
                float x1 = x0 + 16;
                float y0 = minY + yTile * 16;
                float y1 = y0 + 16;

                float u0 = 1.0f - (float)xTile / tilesX;
                float u1 = 1.0f - (float)(xTile + 1) / tilesX;
                float v0 = 1.0f - (float)yTile / tilesY;
                float v1 = 1.0f - (float)(yTile + 1) / tilesY;

                imageConsumer.vertex(matrix, x1, y0, -0.5F).color(255,255,255,255).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
                imageConsumer.vertex(matrix, x0, y0, -0.5F).color(255,255,255,255).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
                imageConsumer.vertex(matrix, x0, y1, -0.5F).color(255,255,255,255).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
                imageConsumer.vertex(matrix, x1, y1, -0.5F).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
            }
        }

        TextureAtlasSprite backSprite = Minecraft.getInstance().getPaintingTextures().getBackSprite();

        VertexConsumer frameConsumer = buffer.getBuffer(RenderType.entitySolid(backSprite.atlasLocation()));

        float i = backSprite.getU0();
        float j = backSprite.getU1();
        float k = backSprite.getV0();
        float l = backSprite.getV1();
        float m = backSprite.getU0();
        float n = backSprite.getU1();
        float o = backSprite.getV0();
        float p = backSprite.getV(1.0F);
        float q = backSprite.getU0();
        float r = backSprite.getU(1.0F);
        float s = backSprite.getV0();
        float t = backSprite.getV1();

        for (int xTile = 0; xTile < tilesX; xTile++) {
            for (int yTile = 0; yTile < tilesY; yTile++) {
                float x0 = minX + xTile * 16;
                float x1 = x0 + 16;
                float y0 = minY + yTile * 16;
                float y1 = y0 + 16;

                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y1, j, k, 0.5F, 0, 0, 1, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y1, i, k, 0.5F, 0, 0, 1, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y0, i, l, 0.5F, 0, 0, 1, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y0, j, l, 0.5F, 0, 0, 1, light);

                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y1, m, o, -0.5F, 0, 1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y1, n, o, -0.5F, 0, 1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y1, n, p, 0.5F, 0, 1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y1, m, p, 0.5F, 0, 1, 0, light);

                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y0, m, o, 0.5F, 0, -1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y0, n, o, 0.5F, 0, -1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y0, n, p, -0.5F, 0, -1, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y0, m, p, -0.5F, 0, -1, 0, light);

                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y1, r, s, 0.5F, -1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y0, r, t, 0.5F, -1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y0, q, t, -0.5F, -1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x1, y1, q, s, -0.5F, -1, 0, 0, light);

                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y1, r, s, -0.5F, 1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y0, r, t, -0.5F, 1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y0, q, t, 0.5F, 1, 0, 0, light);
                this.convenientImage$vertex(matrix, normal, frameConsumer, x0, y1, q, s, 0.5F, 1, 0, 0, light);
            }
        }

        poseStack.popPose();
        super.render(painting, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Unique
    private void convenientImage$vertex(Matrix4f pose, Matrix3f normal, @NotNull VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV) {
        consumer.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightmapUV).normal(normal, normalX, normalY, normalZ).endVertex();
    }
}
