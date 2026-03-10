package me.kall.dragit.callback.invoker;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.itemframe.ItemFrameSavePacket;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;

public class ItemFrameDropInvoker implements DragCallback.Invoker {
    private static final Direction[] X_NEIGHBORS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
    private static final Direction[] Z_NEIGHBORS = {Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
    private static final Direction[] Y_NEIGHBORS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Override
    public boolean invoke(long window, int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        HitResult target = minecraft.hitResult;

        if (level == null || target == null) return false;
        if (target.getType() != HitResult.Type.ENTITY) return false;
        if (!(((EntityHitResult) target).getEntity() instanceof ItemFrame targetFrame)) return false;

        Direction facing = targetFrame.getDirection();

        try {
            Long2ObjectMap<int[]> frameGrid = findConnectedFrames(level, targetFrame.blockPosition().asLong(), facing);
            BoundingBox box = resolveFromBlocks(frameGrid.keySet());

            int totalColumns = getTotalColumns(facing, box);
            int totalRows = getTotalRows(facing, box);

            long[] positions = new long[frameGrid.size()];
            int[] columns = new int[frameGrid.size()];
            int[] rows = new int[frameGrid.size()];
            fillFrameArrays(frameGrid, box, facing, positions, columns, rows);

            byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getItemFrameMaxPixels());
            ResourceLocation textureLocation = ImageCache.getOrCreate(textureBytes);

            ResourceLocation dimension = level.dimension().location();
            ClientItemFrames.registerGroup(dimension, positions, columns, rows, totalColumns, totalRows, textureLocation, textureBytes);
            if (DragItClient.canSync()) {
                DragNetworker.INSTANCE.sendToServer(new ItemFrameSavePacket(dimension, textureLocation, textureBytes, positions, columns, rows, totalColumns, totalRows));
            }

            DragIt.LOGGER.info("Item-frame image applied: {} frames, grid {}x{}", frameGrid.size(), totalColumns, totalRows);
            return true;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering item-frame image", exception);
            return false;
        }
    }

    private static int getTotalColumns(@NotNull Direction facing, BoundingBox box) {
        return switch (facing) {
            case NORTH, SOUTH, UP, DOWN -> box.maxX() - box.minX() + 1;
            case EAST, WEST -> box.maxZ() - box.minZ() + 1;
        };
    }

    private static int getTotalRows(@NotNull Direction facing, BoundingBox box) {
        return switch (facing) {
            case NORTH, SOUTH, EAST, WEST -> box.maxY() - box.minY() + 1;
            case UP, DOWN -> box.maxZ() - box.minZ() + 1;
        };
    }

    private static void fillFrameArrays(@NotNull Long2ObjectMap<int[]> frameGrid, @NotNull BoundingBox box, Direction facing, long[] positions, int[] columns, int[] rows) {
        int index = 0;
        for (Long2ObjectMap.Entry<int[]> entry : frameGrid.long2ObjectEntrySet()) {
            long packed = entry.getLongKey();
            int x = BlockPos.getX(packed), y = BlockPos.getY(packed), z = BlockPos.getZ(packed);
            int column, row;
            switch (facing) {
                case NORTH -> {
                    column = box.maxX() - x;
                    row = box.maxY() - y;
                }
                case SOUTH -> {
                    column = x - box.minX();
                    row = box.maxY() - y;
                }
                case EAST -> {
                    column = box.maxZ() - z;
                    row = box.maxY() - y;
                }
                case WEST -> {
                    column = z - box.minZ();
                    row = box.maxY() - y;
                }
                case UP -> {
                    column = x - box.minX();
                    row = z - box.minZ();
                }
                case DOWN -> {
                    column = x - box.minX();
                    row = box.maxZ() - z;
                }
                default -> {
                    column = 0;
                    row = 0;
                }
            }

            entry.setValue(new int[]{column, row});

            positions[index] = packed;
            columns[index] = column;
            rows[index] = row;
            index++;
        }
    }

    @Contract("_ -> new")
    private static @NotNull BoundingBox resolveFromBlocks(@NotNull LongSet blocks) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (long packed : blocks) {
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static @NotNull Long2ObjectMap<int[]> findConnectedFrames(ClientLevel level, long startPosition, Direction facing) {
        Long2ObjectMap<int[]> frameGrid = new Long2ObjectLinkedOpenHashMap<>();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();

        queue.enqueue(startPosition);
        frameGrid.put(startPosition, null);

        while (!queue.isEmpty()) {
            long current = queue.dequeueLong();
            Direction[] neighbors = switch (facing.getAxis()) {
                case X -> X_NEIGHBORS; case Z -> Z_NEIGHBORS; case Y -> Y_NEIGHBORS;
            };
            for (Direction neighbor : neighbors) resolveNeighbor(frameGrid, queue, level, facing, BlockPos.offset(current, neighbor));
        }
        return frameGrid;
    }

    private static void resolveNeighbor(@NotNull Long2ObjectMap<int[]> frameGrid, LongArrayFIFOQueue queue, ClientLevel level, Direction facing, long neighbor) {
        if (!frameGrid.containsKey(neighbor) && hasItemFrameAt(level, neighbor, facing)) {
            frameGrid.put(neighbor, null);
            queue.enqueue(neighbor);
        }
    }

    private static boolean hasItemFrameAt(@NotNull ClientLevel level, long position, Direction facing) {
        int x = BlockPos.getX(position);
        int y = BlockPos.getY(position);
        int z = BlockPos.getZ(position);
        AABB boundingBox = new AABB(x, y, z, x + 1, y + 1, z + 1);
        return !level.getEntitiesOfClass(ItemFrame.class, boundingBox, frame -> frame.blockPosition().asLong() == position && frame.getDirection() == facing).isEmpty();
    }
}