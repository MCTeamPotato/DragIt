package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.painting.ClientPaintings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingRenderer.class)
public abstract class PaintingRendererMixin extends EntityRenderer<Painting> {
    @Unique private final ThreadLocal<MultiBufferSource> dropIt$buffer = new ThreadLocal<>();

    protected PaintingRendererMixin(EntityRenderDispatcher arg) {
        super(arg);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/Painting;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void validateBufferSource(Painting entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        this.dropIt$buffer.set(buffer);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/decoration/Painting;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void invalidateBufferSource(Painting entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        this.dropIt$buffer.remove();
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/decoration/Painting;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/PaintingRenderer;renderPainting(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/decoration/Painting;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void dropIt$renderDropped(PaintingRenderer paintingRenderer, PoseStack pose, VertexConsumer consumer, @NotNull Painting entity, int width, int height, TextureAtlasSprite paintingSprite, TextureAtlasSprite backSprite, Operation<Void> original) {
        ClientTextureData clientTextureData = ClientPaintings.PAINTINGS.getOrDefault(entity.level.dimension().location(), Long2ObjectMaps.emptyMap()).get(entity.blockPosition().asLong());
        if (clientTextureData == null) {
            original.call(paintingRenderer, pose, consumer, entity, width, height, paintingSprite, backSprite);
        } else {
            PoseStack.Pose last = pose.last();
            Matrix4f matrix4f = last.pose();
            Matrix3f matrix3f = last.normal();
            this.dropIt$renderBack(width, height, backSprite, entity, matrix4f, matrix3f, consumer);
            this.dropIt$renderImage(width, height, clientTextureData, entity, matrix4f, matrix3f);
        }
    }

    @Unique
    private MultiBufferSource dropIt$getBuffer() {
        MultiBufferSource localSource = this.dropIt$buffer.get();
        if (localSource != null) return localSource;
        Minecraft minecraft = Minecraft.getInstance();
        RenderBuffers renderBuffers = minecraft.renderBuffers();
        return ((LevelRendererInvoker)minecraft.levelRenderer).shouldShowEntityOutlines() ? renderBuffers.outlineBufferSource() : renderBuffers.bufferSource();
    }

    @Unique
    private void dropIt$renderImage(int width, int height, ClientTextureData clientTextureData, @NotNull Painting entity, Matrix4f pose, Matrix3f normal) {
        if (clientTextureData == null) return;
        float minX = -width / 2f;
        float minY = -height / 2f;
        int tilesX = width / 16;
        int tilesY = height / 16;

        VertexConsumer imageConsumer = this.dropIt$getBuffer().getBuffer(RenderType.entitySolid(clientTextureData.textureLocation()));
        int light = LevelRenderer.getLightColor(entity.level, entity.blockPosition());

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

                this.vertex(pose, normal, imageConsumer, x1, y0, u1, v0, -0.5F, 0, 0, -1, light);
                this.vertex(pose, normal, imageConsumer, x0, y0, u0, v0, -0.5F, 0, 0, -1, light);
                this.vertex(pose, normal, imageConsumer, x0, y1, u0, v1, -0.5F, 0, 0, -1, light);
                this.vertex(pose, normal, imageConsumer, x1, y1, u1, v1, -0.5F, 0, 0, -1, light);
            }
        }
    }

    @Unique
    private void dropIt$renderBack(int width, int height, @NotNull TextureAtlasSprite backSprite, Painting painting, Matrix4f pose, Matrix3f normal, VertexConsumer backConsumer) {
        float minX = -width / 2.0F;
        float minY = -height / 2.0F;

        float backU0 = backSprite.getU0();
        float backU1 = backSprite.getU1();
        float backV0 = backSprite.getV0();
        float backV1 = backSprite.getV1();

        float topU0 = backSprite.getU0();
        float topU1 = backSprite.getU1();
        float topV0 = backSprite.getV0();
        float topV1 = backSprite.getV(1.0);

        float sideU0 = backSprite.getU0();
        float sideU1 = backSprite.getU(1.0);
        float sideV0 = backSprite.getV0();
        float sideV1 = backSprite.getV1();

        int tilesX = width / 16;
        int tilesY = height / 16;

        for (int tileX = 0; tileX < tilesX; tileX++) {
            for (int tileY = 0; tileY < tilesY; tileY++) {
                float x1 = minX + (tileX + 1) * 16;
                float x0 = minX + tileX * 16;
                float y1 = minY + (tileY + 1) * 16;
                float y0 = minY + tileY * 16;

                int blockX = painting.blockPosition().getX();
                int blockY = Mth.floor(painting.getY() + (y1 + y0) / 2.0F / 16.0F);
                int blockZ = painting.blockPosition().getZ();

                Direction direction = painting.getDirection();
                if (direction == Direction.NORTH) blockX = Mth.floor(painting.getX() + (x1 + x0) / 2.0F / 16.0F);
                if (direction == Direction.WEST)  blockZ = Mth.floor(painting.getZ() - (x1 + x0) / 2.0F / 16.0F);
                if (direction == Direction.SOUTH) blockX = Mth.floor(painting.getX() - (x1 + x0) / 2.0F / 16.0F);
                if (direction == Direction.EAST)  blockZ = Mth.floor(painting.getZ() + (x1 + x0) / 2.0F / 16.0F);

                int light = LevelRenderer.getLightColor(painting.level, new BlockPos(blockX, blockY, blockZ));

                this.vertex(pose, normal, backConsumer, x1, y1, backU1, backV0, 0.5F,  0,  0,  1, light);
                this.vertex(pose, normal, backConsumer, x0, y1, backU0, backV0, 0.5F,  0,  0,  1, light);
                this.vertex(pose, normal, backConsumer, x0, y0, backU0, backV1, 0.5F,  0,  0,  1, light);
                this.vertex(pose, normal, backConsumer, x1, y0, backU1, backV1, 0.5F,  0,  0,  1, light);

                this.vertex(pose, normal, backConsumer, x1, y1, topU0, topV0, -0.5F,  0,  1,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y1, topU1, topV0, -0.5F,  0,  1,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y1, topU1, topV1,  0.5F,  0,  1,  0, light);
                this.vertex(pose, normal, backConsumer, x1, y1, topU0, topV1,  0.5F,  0,  1,  0, light);

                this.vertex(pose, normal, backConsumer, x1, y0, topU0, topV0,  0.5F,  0, -1,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y0, topU1, topV0,  0.5F,  0, -1,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y0, topU1, topV1, -0.5F,  0, -1,  0, light);
                this.vertex(pose, normal, backConsumer, x1, y0, topU0, topV1, -0.5F,  0, -1,  0, light);

                this.vertex(pose, normal, backConsumer, x1, y1, sideU1, sideV0,  0.5F, -1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x1, y0, sideU1, sideV1,  0.5F, -1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x1, y0, sideU0, sideV1, -0.5F, -1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x1, y1, sideU0, sideV0, -0.5F, -1,  0,  0, light);

                this.vertex(pose, normal, backConsumer, x0, y1, sideU1, sideV0, -0.5F,  1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y0, sideU1, sideV1, -0.5F,  1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y0, sideU0, sideV1,  0.5F,  1,  0,  0, light);
                this.vertex(pose, normal, backConsumer, x0, y1, sideU0, sideV0,  0.5F,  1,  0,  0, light);
            }
        }
    }

    @Shadow protected abstract void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int lightmapUV);
    @Shadow public abstract @NotNull ResourceLocation getTextureLocation(@NotNull Painting entity);
}