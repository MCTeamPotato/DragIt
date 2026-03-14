package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.data.cape.VideoCapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Unique private HumanoidModel<@NotNull AvatarRenderState> dragIt$customCapeModel;

    @Unique
    private static final ClientAsset.Texture NOT_NULL = new ClientAsset.Texture() {

        private static final Identifier PLACEHOLDER = Identifier.fromNamespaceAndPath(DragIt.MOD_ID, "placeholder");

        @Override
        public @NotNull Identifier texturePath() {
            return PLACEHOLDER;
        }

        @Override
        public @NotNull Identifier id() {
            return PLACEHOLDER;
        }
    };

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/geom/EntityModelSet;Lnet/minecraft/client/resources/model/EquipmentAssetManager;)V", at = @At("TAIL"))
    private void dragIt$init(CallbackInfo ci) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        meshDefinition.getRoot().clearRecursively();
        meshDefinition.getRoot().getChild("body").addOrReplaceChild("cape", CubeListBuilder.create().texOffs(-1, -1).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, (float) Math.PI, 0.0F));
        this.dragIt$customCapeModel = new PlayerCapeModel(LayerDefinition.create(meshDefinition, 10, 16).bakeRoot());
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;model:Lnet/minecraft/client/model/HumanoidModel;", opcode = Opcodes.GETFIELD))
    private HumanoidModel<@NotNull AvatarRenderState> dragIt$swapModel(CapeLayer capeLayer, Operation<HumanoidModel<@NotNull AvatarRenderState>> original, @Local(argsOnly = true) AvatarRenderState state) {
        return this.dragIt$customCape(state) != null ? this.dragIt$customCapeModel : original.call(capeLayer);
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType dragIt$swapTexture(Identifier texturePath, @NotNull Operation<RenderType> original, @Local(argsOnly = true) AvatarRenderState state) {
        Identifier customCape = this.dragIt$customCape(state);
        return original.call(customCape == null ? texturePath : customCape);
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerSkin;cape()Lnet/minecraft/core/ClientAsset$Texture;"))
    private ClientAsset.Texture skipCapeCheck(PlayerSkin instance, @NotNull Operation<ClientAsset.Texture> original, @Local(argsOnly = true) AvatarRenderState state) {
        return this.dragIt$customCape(state) != null ? NOT_NULL : original.call(instance);
    }

    @Unique
    private @Nullable Identifier dragIt$customCape(AvatarRenderState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(state.id);
            if (entity != null) {
                Identifier videoTexture = VideoCapes.getTexture(entity.getUUID());
                if (videoTexture != null) return videoTexture;
                ClientTextureData cape = ClientCapes.CAPES.get(entity.getUUID());
                if (cape != null) return cape.textureLocation();
            }
        }
        return null;
    }
}