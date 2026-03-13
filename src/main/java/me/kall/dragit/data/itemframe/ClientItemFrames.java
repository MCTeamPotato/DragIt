package me.kall.dragit.data.itemframe;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ClientItemFrames {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<FrameEntry>> ITEM_FRAMES = new Object2ObjectOpenHashMap<>();

    public static void registerGroup(ResourceLocation dimension, long @NotNull [] positions, int[] columns, int[] rows, int totalColumns, int totalRows, ResourceLocation textureLocation, byte[] textureBytes) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        Long2ObjectMap<FrameEntry> dimensionMap = ITEM_FRAMES.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>());
        for (long position : positions) {
            FrameEntry oldEntry = dimensionMap.remove(position);
            if (oldEntry != null && isUnreferenced(dimensionMap, oldEntry.textureLocation())) {
                textureManager.release(oldEntry.textureLocation());
                oldEntry.dynamicTexture().close();
            }
        }

        try {
            NativeImage image = NativeImage.read(textureBytes);
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            textureManager.register(textureLocation, dynamicTexture);

            for (int index = 0; index < positions.length; index++) {
                dimensionMap.put(positions[index], new FrameEntry(textureLocation, dynamicTexture, columns[index], rows[index], totalColumns, totalRows));
            }
            DragIt.LOGGER.info("Registered item-frame image group {} ({} frames, {}x{})", textureLocation, positions.length, totalColumns, totalRows);
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering item-frame image", exception);
        }
    }

    public static @Nullable FrameEntry getFrame(ResourceLocation dimension, long position) {
        Long2ObjectMap<FrameEntry> dimensionMap = ITEM_FRAMES.get(dimension);
        return dimensionMap == null ? null : dimensionMap.get(position);
    }

    private static boolean isUnreferenced(@NotNull Long2ObjectMap<FrameEntry> dimensionMap, ResourceLocation textureLocation) {
        for (FrameEntry entry : dimensionMap.values()) {
            if (entry.textureLocation().equals(textureLocation)) return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void clearAll(ClientPlayerNetworkEvent.LoggingOut event) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Long2ObjectMap<FrameEntry> dimensionMap : ITEM_FRAMES.values()) {
            Object2ObjectMap<ResourceLocation, DynamicTexture> seenTextures = new Object2ObjectOpenHashMap<>();
            for (FrameEntry entry : dimensionMap.values()) {
                seenTextures.put(entry.textureLocation(), entry.dynamicTexture());
            }
            for (Map.Entry<ResourceLocation, DynamicTexture> textureEntry : seenTextures.entrySet()) {
                textureManager.release(textureEntry.getKey());
                textureEntry.getValue().close();
            }
        }
        ITEM_FRAMES.clear();
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        Entity.RemovalReason removalReason = itemFrame.getRemovalReason();
        if (removalReason != Entity.RemovalReason.KILLED && removalReason != Entity.RemovalReason.DISCARDED) return;

        ResourceLocation dimension = level.dimension().location();
        long position = itemFrame.blockPosition().asLong();

        Long2ObjectMap<FrameEntry> frameMap = ITEM_FRAMES.get(dimension);
        if (frameMap == null) return;
        FrameEntry frameEntry = frameMap.get(position);
        if (frameEntry == null) return;

        ResourceLocation textureToRemove = frameEntry.textureLocation();
        frameMap.values().removeIf(e -> e.textureLocation().equals(textureToRemove));

        Minecraft.getInstance().getTextureManager().release(textureToRemove);
        frameEntry.dynamicTexture().close();

        if (frameMap.isEmpty()) ITEM_FRAMES.remove(dimension);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class FrameEntry {
        private final ResourceLocation textureLocation;
        private final DynamicTexture dynamicTexture;
        private final int column;
        private final int row;
        private final int totalColumns;
        private final int totalRows;

        public FrameEntry(ResourceLocation textureLocation, DynamicTexture dynamicTexture, int column, int row, int totalColumns, int totalRows) {
            this.textureLocation = textureLocation;
            this.dynamicTexture = dynamicTexture;
            this.column = column;
            this.row = row;
            this.totalColumns = totalColumns;
            this.totalRows = totalRows;
        }

        public ResourceLocation textureLocation() { return this.textureLocation; }
        public DynamicTexture dynamicTexture() { return this.dynamicTexture; }
        public int column() { return this.column; }
        public int row() { return this.row; }
        public int totalColumns() { return this.totalColumns; }
        public int totalRows() { return this.totalRows; }
    }
}