package me.kall.dragit.network.video;

import me.kall.dragit.DragIt;
import me.kall.dragit.mixin.ServerScreenCheckerInvoker;
import me.kall.dragit.network.base.Handler;
import me.kall.narutoloading.inworld.core.InWorldScreen;
import me.kall.narutoloading.inworld.init.NarutoPackets;
import me.kall.narutoloading.inworld.network.ScreenLifePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongPredicate;

public class VideoScreenPacket extends Handler {
    private final int leftBottom;
    private final int rightBottom;
    private final String video;

    public VideoScreenPacket(int leftBottom, int rightBottom, String video) {
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
        this.video = video;
    }

    public VideoScreenPacket(@NotNull FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readUtf());
    }

    public void save(@NotNull FriendlyByteBuf buffer)  {
        buffer.writeInt(this.leftBottom);
        buffer.writeInt(this.rightBottom);
        buffer.writeUtf(this.video);
    }

    public void handle(ServerPlayer player) {
        try {
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            if (!(level.getEntity(this.leftBottom) instanceof ItemFrame leftBottomFrame)) return;
            if (!(level.getEntity(this.rightBottom) instanceof ItemFrame rightBottomFrame)) return;

            Direction facing = leftBottomFrame.getDirection();

            BlockPos lastCorner = leftBottomFrame.blockPosition().relative(facing.getOpposite());
            BlockPos currentCorner = rightBottomFrame.blockPosition().relative(facing.getOpposite());

            LongPredicate predicate = posLong -> ServerScreenCheckerInvoker.isHangingEntityAt(level, BlockPos.of(posLong).relative(facing).asLong());

            InWorldScreen screen = ServerScreenCheckerInvoker.tryBuildScreen(player, level, lastCorner, currentCorner, predicate);

            if (screen != null) {
                screen.setPath(this.video, this.video);
                NarutoPackets.INSTANCE.send(PacketDistributor.ALL.noArg(), new ScreenLifePacket(screen, true));
                NarutoPackets.INSTANCE.send(PacketDistributor.ALL.noArg(), new ScreenLifePacket(screen, false));
                ServerScreenCheckerInvoker.setHangingEntitiesInvisible(level, screen, facing, true);
            }
        } catch (Exception exception) {
            DragIt.LOGGER.error("Error handling VideoScreenPacket", exception);
        }
    }
}