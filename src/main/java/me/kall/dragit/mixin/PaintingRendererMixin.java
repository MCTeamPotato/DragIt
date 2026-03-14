package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.painting.ClientPaintings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.painting.Painting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin extends EntityRenderer<@NotNull Painting, @NotNull PaintingRenderState> {

    protected PaintingRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/PaintingRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;renderPainting(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/rendertype/RenderType;[IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void dropIt$renderDropped(PaintingRenderer paintingRenderer, PoseStack pose, SubmitNodeCollector nodeCollector, RenderType renderType, int[] lights, int width, int height, TextureAtlasSprite frontSprite, TextureAtlasSprite backSprite, Operation<Void> original, @Local(argsOnly = true) PaintingRenderState paintingRenderState) {
        ClientTextureData clientTextureData = null;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            int x = Mth.floor(paintingRenderState.x);
            int y = Mth.floor(paintingRenderState.y);
            int z = Mth.floor(paintingRenderState.z);
            clientTextureData = (ClientTextureData) ((Long2ObjectMap<?>) ClientPaintings.PAINTINGS.getOrDefault(
                    level.dimension().identifier(), Long2ObjectMaps.emptyMap())).get(BlockPos.asLong(x, y, z));
        }

        if (clientTextureData == null) {
            original.call(paintingRenderer, pose, nodeCollector, renderType, lights, width, height, frontSprite, backSprite);
        } else {
            dropIt$renderBack(width, height, backSprite, lights, pose, nodeCollector);
            dropIt$renderImage(width, height, clientTextureData, lights, pose, nodeCollector);
        }
    }

    @Unique
    private void dropIt$renderImage(int width, int height, ClientTextureData clientTextureData, int[] lights, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        if (clientTextureData == null) return;
        RenderType renderType = RenderTypes.entitySolidZOffsetForward(clientTextureData.textureLocation());

        nodeCollector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            float minX = -width / 2.0F;
            float minY = -height / 2.0F;
            for (int xTile = 0; xTile < width; xTile++) {
                for (int yTile = 0; yTile < height; yTile++) {
                    float x0 = minX + xTile;
                    float x1 = x0 + 1.0F;
                    float y0 = minY + yTile;
                    float y1 = y0 + 1.0F;
                    int light = lights[xTile + yTile * width];
                    float u0 = 1.0F - (float) xTile / width;
                    float u1 = 1.0F - (float) (xTile + 1) / width;
                    float v0 = 1.0F - (float) yTile / height;
                    float v1 = 1.0F - (float) (yTile + 1) / height;
                    vertex(pose, consumer, x1, y0, u1, v0, -0.03125F, 0, 0, -1, light);
                    vertex(pose, consumer, x0, y0, u0, v0, -0.03125F, 0, 0, -1, light);
                    vertex(pose, consumer, x0, y1, u0, v1, -0.03125F, 0, 0, -1, light);
                    vertex(pose, consumer, x1, y1, u1, v1, -0.03125F, 0, 0, -1, light);
                }
            }
        });
    }

    @Unique
    private void dropIt$renderBack(int width, int height, @NotNull TextureAtlasSprite backSprite, int[] lights, PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector) {
        RenderType renderType = RenderTypes.entitySolidZOffsetForward(backSprite.atlasLocation());

        nodeCollector.submitCustomGeometry(poseStack, renderType, (pose, backConsumer) -> {
            float minX = -width / 2.0F;
            float minY = -height / 2.0F;
            float backU0 = backSprite.getU0();
            float backU1 = backSprite.getU1();
            float backV0 = backSprite.getV0();
            float backV1 = backSprite.getV1();
            float topU0  = backSprite.getU0();
            float topU1  = backSprite.getU1();
            float topV0  = backSprite.getV0();
            float topV1  = backSprite.getV(0.0625F);
            float sideU0 = backSprite.getU0();
            float sideU1 = backSprite.getU(0.0625F);
            float sideV0 = backSprite.getV0();
            float sideV1 = backSprite.getV1();
            for (int tileX = 0; tileX < width; tileX++) {
                for (int tileY = 0; tileY < height; tileY++) {
                    float x0 = minX + tileX;
                    float x1 = x0 + 1.0F;
                    float y0 = minY + tileY;
                    float y1 = y0 + 1.0F;
                    int light = lights[tileX + tileY * width];

                    vertex(pose, backConsumer, x1, y1, backU1, backV0,  0.03125F,  0,  0,  1, light);
                    vertex(pose, backConsumer, x0, y1, backU0, backV0,  0.03125F,  0,  0,  1, light);
                    vertex(pose, backConsumer, x0, y0, backU0, backV1,  0.03125F,  0,  0,  1, light);
                    vertex(pose, backConsumer, x1, y0, backU1, backV1,  0.03125F,  0,  0,  1, light);

                    vertex(pose, backConsumer, x1, y1, topU0,  topV0,  -0.03125F, 0,  1,  0, light);
                    vertex(pose, backConsumer, x0, y1, topU1,  topV0,  -0.03125F, 0,  1,  0, light);
                    vertex(pose, backConsumer, x0, y1, topU1,  topV1,   0.03125F, 0,  1,  0, light);
                    vertex(pose, backConsumer, x1, y1, topU0,  topV1,   0.03125F, 0,  1,  0, light);

                    vertex(pose, backConsumer, x1, y0, topU0,  topV0,   0.03125F, 0, -1,  0, light);
                    vertex(pose, backConsumer, x0, y0, topU1,  topV0,   0.03125F, 0, -1,  0, light);
                    vertex(pose, backConsumer, x0, y0, topU1,  topV1,  -0.03125F, 0, -1,  0, light);
                    vertex(pose, backConsumer, x1, y0, topU0,  topV1,  -0.03125F, 0, -1,  0, light);

                    vertex(pose, backConsumer, x1, y1, sideU1, sideV0,  0.03125F, -1,  0,  0, light);
                    vertex(pose, backConsumer, x1, y0, sideU1, sideV1,  0.03125F, -1,  0,  0, light);
                    vertex(pose, backConsumer, x1, y0, sideU0, sideV1, -0.03125F, -1,  0,  0, light);
                    vertex(pose, backConsumer, x1, y1, sideU0, sideV0, -0.03125F, -1,  0,  0, light);

                    vertex(pose, backConsumer, x0, y1, sideU1, sideV0, -0.03125F,  1,  0,  0, light);
                    vertex(pose, backConsumer, x0, y0, sideU1, sideV1, -0.03125F,  1,  0,  0, light);
                    vertex(pose, backConsumer, x0, y0, sideU0, sideV1,  0.03125F,  1,  0,  0, light);
                    vertex(pose, backConsumer, x0, y1, sideU0, sideV0,  0.03125F,  1,  0,  0, light);
                }
            }
        });
    }

    @Shadow
    protected abstract void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light);
}