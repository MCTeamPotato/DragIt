package me.kall.dragit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragCommonConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@EventBusSubscriber(modid = DragIt.MOD_ID)
public class DragConfigCommand {
    @SubscribeEvent
    public static void onRegisterCommands(@NotNull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("dragit")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.literal("whitelist")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(ctx -> executeWhitelistAdd(ctx, EntityArgument.getPlayers(ctx, "players")))
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(ctx -> executeWhitelistRemove(ctx, EntityArgument.getPlayers(ctx, "players")))
                                        )
                                )
                        )
                        .then(Commands.literal("blacklist")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(ctx -> executeBlacklistAdd(ctx, EntityArgument.getPlayers(ctx, "players")))
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(ctx -> executeBlacklistRemove(ctx, EntityArgument.getPlayers(ctx, "players")))
                                        )
                                )
                        )
        );
    }

    private static int executeWhitelistAdd(CommandContext<CommandSourceStack> ctx, @NotNull Collection<ServerPlayer> players) {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        int count = 0;
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            if (config.whitelisted(uuid)) {
                ctx.getSource().sendFailure(Component.translatable("commands.dragit.whitelist.add.already", player.getName()));
            } else {
                config.setWhitelisted(uuid);
                ctx.getSource().sendSuccess(() -> Component.translatable("commands.dragit.whitelist.add.success", player.getName()), true);
                count++;
            }
        }
        return count;
    }

    private static int executeWhitelistRemove(CommandContext<CommandSourceStack> ctx, @NotNull Collection<ServerPlayer> players) {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        int count = 0;
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            if (!config.whitelisted(uuid)) {
                ctx.getSource().sendFailure(Component.translatable("commands.dragit.whitelist.remove.not_found", player.getName()));
            } else {
                config.removeWhitelisted(uuid);
                ctx.getSource().sendSuccess(() -> Component.translatable("commands.dragit.whitelist.remove.success", player.getName()), true);
                count++;
            }
        }
        return count;
    }

    private static int executeBlacklistAdd(CommandContext<CommandSourceStack> ctx, @NotNull Collection<ServerPlayer> players) {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        int count = 0;
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            if (config.blacklisted(uuid)) {
                ctx.getSource().sendFailure(Component.translatable("commands.dragit.blacklist.add.already", player.getName()));
            } else {
                config.setBlacklisted(uuid);
                ctx.getSource().sendSuccess(() -> Component.translatable("commands.dragit.blacklist.add.success", player.getName()), true);
                count++;
            }
        }
        return count;
    }

    private static int executeBlacklistRemove(CommandContext<CommandSourceStack> ctx, @NotNull Collection<ServerPlayer> players) {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        int count = 0;
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            if (!config.blacklisted(uuid)) {
                ctx.getSource().sendFailure(Component.translatable("commands.dragit.blacklist.remove.not_found", player.getName()));
            } else {
                config.removeBlacklisted(uuid);
                ctx.getSource().sendSuccess(() -> Component.translatable("commands.dragit.blacklist.remove.success", player.getName()), true);
                count++;
            }
        }
        return count;
    }
}