package me.kall.dragit.mixin;

import com.google.common.collect.Lists;
import me.kall.dragit.ext.ImageContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements ImageContainer {
    @Shadow protected EditBox input;
    @Unique private final List<String> dropIt$images = Lists.newArrayList();

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Override
    public void dropIt$addImage(String filePath) {
        this.dropIt$images.add(filePath);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderAttached(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.dropIt$images.isEmpty()) {
            guiGraphics.fill(2, this.input.getY() - 12, this.width - 2, this.height - 14, Minecraft.getInstance().options.getBackgroundColor(Integer.MIN_VALUE));
            guiGraphics.drawString(this.font, Component.translatable("gui.dragit.images", Arrays.toString(this.dropIt$images.toArray())), this.input.getX(), this.input.getY() - 12, 14737632, false);
        }
    }
}
