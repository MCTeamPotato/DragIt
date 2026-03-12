package me.kall.dragit.mixin;

import me.kall.narutoloading.inworld.core.InWorldScreen;
import me.kall.narutoloading.inworld.core.ServerScreenChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.LongPredicate;

@Mixin(value = ServerScreenChecker.class, remap = false)
public interface ServerScreenCheckerInvoker {
    @Invoker("tryBuildScreen")
    static InWorldScreen tryBuildScreen(@NotNull ServerPlayer player, @NotNull ServerLevel level, @NotNull BlockPos lastCorner, @NotNull BlockPos currentCorner, @NotNull LongPredicate borderPredicate) {
        throw new RuntimeException();
    }

    @Invoker("setHangingEntitiesInvisible")
    static void setHangingEntitiesInvisible(@NotNull ServerLevel level, @NotNull InWorldScreen screen, @NotNull Direction facing, boolean invisible) {
        throw new RuntimeException();
    }

    @Invoker("isHangingEntityAt")
    static boolean isHangingEntityAt(@NotNull ServerLevel level, long entityPosLong){
        throw new RuntimeException();
    }
}
