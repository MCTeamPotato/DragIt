package me.kall.dragit;

import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Mod(DragIt.MOD_ID)
@EventBusSubscriber(modid = DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt(@NotNull IEventBus modBus, @NotNull Dist dist, ModContainer container) {
        modBus.addListener(DragNetworker::register);
        DragCommonConfig.INSTANCE.register(container, modBus);
        if (dist.isClient()) {
            DragClientConfig.INSTANCE.register(container);
        }
    }

    @SubscribeEvent
    public static void giveGuide(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag persistentData = player.getPersistentData();
            boolean guided = persistentData.getBoolean("DragItGuided");
            if (guided) return;
            persistentData.putBoolean("DragItGuided", true);

            ItemStack paper = Items.PAPER.getDefaultInstance();

            CompoundTag dragItGuide = new CompoundTag();
            dragItGuide.putBoolean("DragItGuide", true);

            paper.set(DataComponents.CUSTOM_DATA, CustomData.of(dragItGuide));
            paper.set(DataComponents.CUSTOM_NAME, GuideComponents.PAPER);

            if (!player.addItem(paper)) player.drop(paper, false);
        }
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
