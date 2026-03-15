package me.kall.dragit;

import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(DragIt.MOD_ID)
@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt() {
        ModLoadingContext context = ModLoadingContext.get();
        DragNetworker.register();
        DragCommonConfig.INSTANCE.register(context, FMLJavaModLoadingContext.get().getModEventBus());
        if (FMLLoader.getDist().isClient()) {
            DragClientConfig.INSTANCE.register(context);
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

            paper.setTag(dragItGuide);
            paper.setHoverName(GuideComponents.PAPER);

            if (!player.addItem(paper)) player.drop(paper, false);
        }
    }
}
