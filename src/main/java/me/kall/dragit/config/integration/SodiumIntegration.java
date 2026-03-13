package me.kall.dragit.config.integration;

import com.google.common.collect.ImmutableList;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.api.options.OptionIdentifier;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
import org.embeddedt.embeddium.api.options.control.SliderControl;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionImpact;
import org.embeddedt.embeddium.api.options.structure.OptionImpl;
import org.embeddedt.embeddium.api.options.structure.OptionPage;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.gui.options.storage.EmbeddiumOptionsStorage;
import org.jetbrains.annotations.NotNull;

public class SodiumIntegration {
    private static final EmbeddiumOptionsStorage sodiumOpts = new EmbeddiumOptionsStorage();

    private static void addConfigPage(@NotNull OptionGUIConstructionEvent event) {
        OptionImpl<EmbeddiumOptions, Integer> chatWidth = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_width"))
                .setName(Component.translatable("config.dragit.chat_width.name"))
                .setTooltip(Component.translatable("config.dragit.chat_width.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxWidth(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxWidth())
                .build();

        OptionImpl<EmbeddiumOptions, Integer> chatHeight = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_height"))
                .setName(Component.translatable("config.dragit.chat_height.name"))
                .setTooltip(Component.translatable("config.dragit.chat_height.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxHeight(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxHeight())
                .build();

        OptionImpl<EmbeddiumOptions, Integer> chatPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_pixels"))
                .setName(Component.translatable("config.dragit.chat_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.chat_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup chatGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_group")).add(chatWidth).add(chatHeight).add(chatPixels).build();

        OptionImpl<EmbeddiumOptions, Integer> paintingPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_pixels"))
                .setName(Component.translatable("config.dragit.painting_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.painting_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setPaintingMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getPaintingMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup paintingGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_group")).add(paintingPixels).build();

        OptionImpl<EmbeddiumOptions, Integer> itemFramePixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "item_frame_pixels"))
                .setName(Component.translatable("config.dragit.item_frame_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.item_frame_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setItemFrameMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getItemFrameMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup itemFrameGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "item_frame_group")).add(itemFramePixels).build();

        OptionImpl<EmbeddiumOptions, Integer> capePixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "cape_pixels"))
                .setName(Component.translatable("config.dragit.cape_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.cape_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setCapeMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getCapeMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup capeGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "cape_group")).add(capePixels).build();

        ImmutableList<OptionGroup> groups = ImmutableList.of(chatGroup, paintingGroup, itemFrameGroup, capeGroup);
        event.addPage(new OptionPage(OptionIdentifier.create(DragIt.MOD_ID, "client_config"), Component.translatable("config.dragit.page.chat"), groups));
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SodiumIntegration::addConfigPage);
    }
}