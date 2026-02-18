package me.kall.dragit.data;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public record ClientTextureData(ResourceLocation textureLocation, DynamicTexture dynamicTexture) {}
