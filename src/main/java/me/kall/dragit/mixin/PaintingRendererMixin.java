package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import me.kall.dragit.data.painting.ClientPaintings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.decoration.Painting;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin extends EntityRenderer<Painting> {
    @Unique private final ThreadLocal<Boolean> dropIt$usingClientPainting = new ThreadLocal<>();

    protected PaintingRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow protected abstract void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV);

    @Inject(method = "renderPainting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;getU0()F", ordinal = 0))
    private void renderDropped(PoseStack poseStack, VertexConsumer consumer, @NotNull Painting entity, int width, int height, TextureAtlasSprite paintingSprite, TextureAtlasSprite backSprite, CallbackInfo ci, @Local PoseStack.Pose pose, @Local Matrix4f matrix4f, @Local Matrix3f matrix3f) {
        ClientPaintings.Painting painting = ClientPaintings.PAINTINGS.getOrDefault(entity.level().dimension().location(), Long2ObjectMaps.emptyMap()).get(entity.blockPosition().asLong());
        this.dropIt$usingClientPainting.set(painting != null);
        this.dropIt$render(width, height, painting, entity, matrix4f, matrix3f);
    }

    @Unique
    private void dropIt$render(int width, int height, ClientPaintings.Painting painting, @NotNull Painting entity, Matrix4f matrix4f, Matrix3f matrix3f) {
        if (painting == null) return;
        float minX = -width / 2f;
        float minY = -height / 2f;
        int tilesX = width / 16;
        int tilesY = height / 16;

        VertexConsumer imageConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entitySolid(painting.textureLocation()));
        int light = LevelRenderer.getLightColor(entity.level(), entity.blockPosition());

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

                this.vertex(matrix4f, matrix3f, imageConsumer, x1, y0, u1, v0, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrix3f, imageConsumer, x0, y0, u0, v0, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrix3f, imageConsumer, x0, y1, u0, v1, -0.5F, 0, 0, -1, light);
                this.vertex(matrix4f, matrix3f, imageConsumer, x1, y1, u1, v1, -0.5F, 0, 0, -1, light);
            }
        }
    }

    @WrapOperation(method = "renderPainting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;vertex(Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFIIII)V", ordinal = 0))
    private void skipOrigin1(PaintingRenderer instance, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV, Operation<Void> original) {
        if (this.dropIt$usingClientPainting.get()) return;
        original.call(instance, pose, normal, consumer, x, y, u, v, z, normalX, normalY, normalZ, lightmapUV);
    }

    @WrapOperation(method = "renderPainting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;vertex(Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFIIII)V", ordinal = 1))
    private void skipOrigin2(PaintingRenderer instance, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV, Operation<Void> original) {
        if (this.dropIt$usingClientPainting.get()) return;
        original.call(instance, pose, normal, consumer, x, y, u, v, z, normalX, normalY, normalZ, lightmapUV);
    }

    @WrapOperation(method = "renderPainting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;vertex(Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFIIII)V", ordinal = 2))
    private void skipOrigin3(PaintingRenderer instance, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV, Operation<Void> original) {
        if (this.dropIt$usingClientPainting.get()) return;
        original.call(instance, pose, normal, consumer, x, y, u, v, z, normalX, normalY, normalZ, lightmapUV);
    }

    @WrapOperation(method = "renderPainting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;vertex(Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFIIII)V", ordinal = 3))
    private void skipOrigin4(PaintingRenderer instance, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV, Operation<Void> original) {
        if (this.dropIt$usingClientPainting.get()) return;
        original.call(instance, pose, normal, consumer, x, y, u, v, z, normalX, normalY, normalZ, lightmapUV);
    }

    @Inject(method = "renderPainting", at = @At("RETURN"))
    private void renderDropped(CallbackInfo ci) {
        this.dropIt$usingClientPainting.remove();
    }
}
