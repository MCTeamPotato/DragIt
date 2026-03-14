package me.kall.dragit.integration;

import me.kall.dragit.DragIt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import vazkii.patchouli.api.PatchouliAPI;

public final class PatchouliIntegration {
    private static final String GIVEN = "drag_it_patchouli_mark";
    private static final ResourceLocation BOOK = new ResourceLocation(DragIt.MOD_ID, "dragit_guide");

    private static void onLogin(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        if (event.getEntity() instanceof ServerPlayer && !player.getPersistentData().getBoolean(GIVEN)) {
            player.getPersistentData().putBoolean(GIVEN, true);
            ItemStack book = PatchouliAPI.get().getBookStack(BOOK);
            if (book.isEmpty()) return;
            if (!player.inventory.add(book)) {
                player.drop(book, false);
            }
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(PatchouliIntegration::onLogin);
    }
}
