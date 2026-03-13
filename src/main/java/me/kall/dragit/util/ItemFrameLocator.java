package me.kall.dragit.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFrameLocator {
    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public static ItemFrame findOtherCorner(@NotNull ItemFrame oneBottomCorner, LocalPlayer player, Level level) {
        BlockPos start = oneBottomCorner.blockPosition();
        Direction facing = oneBottomCorner.getDirection();

        return facing.getAxis().isHorizontal() ? findOtherBottomCorner(start, facing, level) : findNearestEdgeCorner(start, facing, player, level);
    }

    private static ItemFrame findOtherBottomCorner(BlockPos start, @NotNull Direction facing, Level level) {
        Direction left  = facing.getClockWise();
        Direction right = facing.getCounterClockWise();

        BlockPos endLeft  = scanToEnd(start, left,  facing, level);
        BlockPos endRight = scanToEnd(start, right, facing, level);

        BlockPos otherEnd = start.distSqr(endLeft) >= start.distSqr(endRight) ? endLeft : endRight;

        return getItemFrameAt(level, otherEnd, facing);
    }

    private static ItemFrame findNearestEdgeCorner(BlockPos start, Direction facing, LocalPlayer player, Level level) {
        BlockPos cornerA = null, cornerB = null;
        for (Direction dir : DIRECTIONS) {

            BlockPos end = scanToEnd(start, dir, facing, level);
            if (!end.equals(start)) {
                if (cornerA == null) {
                    cornerA = end;
                } else {
                    cornerB = end;
                    break;
                }
            }
        }

        if (cornerA == null) return getItemFrameAt(level, start, facing);
        if (cornerB == null) return getItemFrameAt(level, cornerA, facing);

        Vec3 playerPos = player.position();
        double distA = minDistToSegmentXZ(playerPos, Vec3.atCenterOf(start), Vec3.atCenterOf(cornerA));
        double distB = minDistToSegmentXZ(playerPos, Vec3.atCenterOf(start), Vec3.atCenterOf(cornerB));

        return getItemFrameAt(level, distA <= distB ? cornerA : cornerB, facing);
    }

    private static BlockPos scanToEnd(BlockPos pos, Direction dir, Direction facing, Level level) {
        BlockPos cur  = pos;
        BlockPos next = cur.relative(dir);
        while (hasItemFrame(level, next, facing)) {
            cur = next;
            next = cur.relative(dir);
        }
        return cur;
    }

    private static boolean hasItemFrame(@NotNull Level level, BlockPos pos, Direction facing) {
        return !level.getEntitiesOfClass(ItemFrame.class, new AABB(pos), frame -> frame.getDirection() == facing).isEmpty();
    }

    private static @Nullable ItemFrame getItemFrameAt(@NotNull Level level, BlockPos pos, Direction facing) {
        List<ItemFrame> itemFrames = level.getEntitiesOfClass(ItemFrame.class, new AABB(pos), frame -> frame.getDirection() == facing);
        if (itemFrames.isEmpty()) return null;
        return itemFrames.getFirst();
    }

    private static double minDistToSegmentXZ(Vec3 p, @NotNull Vec3 a, @NotNull Vec3 b) {
        double dx = b.x - a.x;
        double dz = b.z - a.z;
        double lenSq = dx * dx + dz * dz;
        if (lenSq == 0.0) {
            double ex = p.x - a.x, ez = p.z - a.z;
            return Math.sqrt(ex * ex + ez * ez);
        }
        double t = Math.max(0.0, Math.min(1.0, ((p.x - a.x) * dx + (p.z - a.z) * dz) / lenSq));
        double cx = a.x + t * dx;
        double cz = a.z + t * dz;
        double ex = p.x - cx, ez = p.z - cz;
        return Math.sqrt(ex * ex + ez * ez);
    }
}