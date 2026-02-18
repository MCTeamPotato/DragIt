package me.kall.dragit.data;

import net.minecraft.resources.ResourceLocation;

public record SavedTextureData(ResourceLocation textureLocation, byte[] textureBytes) {}
