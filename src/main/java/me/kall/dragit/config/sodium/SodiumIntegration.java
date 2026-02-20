package me.kall.dragit.config.sodium;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragChatConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;
import org.jetbrains.annotations.NotNull;

public class SodiumIntegration {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();

    private static void addConfigPage(@NotNull OptionGUIConstructionEvent event) {
        OptionImpl<SodiumGameOptions, Integer> width = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "width"))
                .setName(Component.translatable("config.dragit.width.name"))
                .setTooltip(Component.translatable("config.dragit.width.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragChatConfig.INSTANCE.setMaxWidth(value), sodiumGameOptions -> DragChatConfig.INSTANCE.getMaxWidth())
                .build();

        OptionImpl<SodiumGameOptions, Integer> height = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "height"))
                .setName(Component.translatable("config.dragit.height.name"))
                .setTooltip(Component.translatable("config.dragit.height.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragChatConfig.INSTANCE.setMaxHeight(value), sodiumGameOptions -> DragChatConfig.INSTANCE.getMaxHeight())
                .build();

        OptionIdentifier<Void> id = OptionIdentifier.create(DragIt.MOD_ID, "chat");
        ImmutableList<OptionGroup> groups = ImmutableList.of(OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_group")).add(width).add(height).build());
        event.addPage(new OptionPage(id, Component.translatable("config.dragit.page.chat"), groups));
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(SodiumIntegration::addConfigPage);
    }
}
