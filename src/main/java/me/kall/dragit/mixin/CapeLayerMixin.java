package me.kall.dragit.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.cape.ClientCapes;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Unique private HumanoidModel<@NotNull AvatarRenderState> dragIt$customCapeModel;

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/geom/EntityModelSet;Lnet/minecraft/client/resources/model/EquipmentAssetManager;)V", at = @At("TAIL"))
    private void dragIt$init(CallbackInfo ci) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        meshDefinition.getRoot().clearRecursively();
        meshDefinition.getRoot().getChild("body").addOrReplaceChild("cape", CubeListBuilder.create().texOffs(-1, -1).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, (float) Math.PI, 0.0F));
        this.dragIt$customCapeModel = new PlayerCapeModel(LayerDefinition.create(meshDefinition, 10, 16).bakeRoot());
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;model:Lnet/minecraft/client/model/HumanoidModel;", opcode = Opcodes.GETFIELD))
    private HumanoidModel<@NotNull AvatarRenderState> dragIt$swapModel(CapeLayer instance, Operation<HumanoidModel<@NotNull AvatarRenderState>> original, @Local(argsOnly = true) AvatarRenderState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(state.id);
            if (entity != null && ClientCapes.CAPES.containsKey(entity.getUUID())) {
                return this.dragIt$customCapeModel;
            }
        }
        return original.call(instance);
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType dragIt$swapTexture(Identifier texturePath, Operation<RenderType> original, @Local(argsOnly = true) AvatarRenderState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(state.id);
            if (entity != null) {
                ClientTextureData cape = ClientCapes.CAPES.get(entity.getUUID());
                if (cape != null) return original.call(cape.textureLocation());
            }
        }
        return original.call(texturePath);
    }
}